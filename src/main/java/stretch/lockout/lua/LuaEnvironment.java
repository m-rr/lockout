package stretch.lockout.lua;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*; // Import base libraries
import org.luaj.vm2.lib.jse.*; // Import JSE specific libraries (to selectively use/omit)
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.game.state.StateResettable;
import stretch.lockout.lua.table.LuaTableBinding;
import stretch.lockout.util.LockoutLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages a sandboxed Lua execution environment for Lockout board scripts.
 * Provides controlled access to Lua libraries and custom Java bindings,
 * restricting potentially dangerous operations like file I/O and OS commands.
 */
public class LuaEnvironment implements EvalFileHandler, StateResettable {
    // Renamed for clarity
    private Globals scriptGlobals;
    private final LockoutSettings settings;
    private final Plugin plugin;
    private final String FILE_EXTENSION = ".lua";
    // Default path, can be changed
    private String environmentPath = "plugins/Lockout/";
    private String libPath = "lib/";
    private final String RESOURCE_IDENTIFIER = "RESOURCE_IDENTIFIER//";
    private final String BOARD_DEFINITION_TABLE = "LockoutBoard";
    // Cache for required files (both filesystem and resource)
    private final Map<String, LuaValue> requiredFilesCache = new HashMap<>();
    // Bindings to be applied to the environment (e.g., tasks, rewards)
    private final List<LuaTableBinding> luaBindings = new ArrayList<>();
    private final boolean isSandboxed;

    /**
     * Creates a new sandboxed LuaEnvironment.
     * Initializes the environment with safe standard libraries and prepares
     * it for loading custom bindings and scripts.
     *
     * @param plugin The Lockout plugin instance.
     * @param settings The Lockout game settings.
     * @param isSandboxed determines sandboxing.
     */
    public LuaEnvironment(@NonNull Plugin plugin, @NonNull LockoutSettings settings, boolean isSandboxed) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.isSandboxed = isSandboxed;
        reset(); // Initialize based on sandboxed flag
    }

    /**
     * Creates a new sandboxed LuaEnvironment.
     * Initializes the environment with safe standard libraries and prepares
     * it for loading custom bindings and scripts.
     *
     * @param plugin The Lockout plugin instance.
     * @param settings The Lockout game settings.
     * @param isSandboxed determines sandboxing.
     * @param bindings {@link List} of {@link LuaTableBinding} to be applied to global table
     */
    public LuaEnvironment(@NonNull Plugin plugin, @NonNull LockoutSettings settings, boolean isSandboxed, List<LuaTableBinding> bindings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.isSandboxed = isSandboxed;
        addLuaTableBindings(bindings);
        reset(); // Initialize based on sandboxed flag
    }

    /**
     * Creates a new Globals instance sandboxed with a limited set of safe libraries.
     * Omits io, os, luajava, and the compiler by default.
     *
     * @author m-rr
     * @return A new, sandboxed Globals object.
     * @since 2.5.2
     */
    private Globals createSandboxedGlobals() {
        Globals globals = new Globals();
        // Load essential safe libraries
        globals.load(new JseBaseLib()); // Basic functions (print, pairs, etc.) & loadfile/load
        globals.load(new PackageLib()); // Needed for 'require'
        globals.load(new Bit32Lib());   // Bitwise operations
        globals.load(new TableLib());   // Table manipulation
        globals.load(new StringLib());  // String manipulation
        //globals.load(new CoroutineLib());// Coroutine support
        globals.load(new JseMathLib()); // Math functions (JSE version)

        LuaValue restrictedAccessHandler = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue table, LuaValue key) {
                String tableName = table.get("restricted_lib_name").optjstring("unknown"); // Get name from dummy table
                String functionName = key.tojstring();
                String errorMessage = String.format(
                        "[Sandbox restriction]: Access to '%s.%s' is prohibited.",
                        tableName,
                        functionName
                );
                throw new RestrictedLuaAccessException(errorMessage); // Throw custom error
            }
        };

        // --- Create Metatable ---
        LuaTable restrictedMetatable = new LuaTable();
        restrictedMetatable.set(LuaValue.INDEX, restrictedAccessHandler); // Set the __index metamethod

        // --- Create Dummy Tables and Assign Metatable ---
        LuaTable dummyIoTable = new LuaTable();
        dummyIoTable.set("restricted_lib_name", LuaValue.valueOf("io")); // Store name for error message
        dummyIoTable.setmetatable(restrictedMetatable); // Assign the metatable
        globals.set("io", dummyIoTable); // Overwrite global 'io'

        LuaTable dummyOsTable = new LuaTable();
        dummyOsTable.set("restricted_lib_name", LuaValue.valueOf("os")); // Store name for error message
        dummyOsTable.setmetatable(restrictedMetatable); // Assign the same metatable
        globals.set("os", dummyOsTable); // Overwrite global 'os'

        LoadState.install(globals);
        LuaC.install(globals);

        return globals;
    }

    /**
     * Applies custom functions (require, _require) and registered LuaTableBindings
     * to the provided Globals object.
     *
     * @param targetGlobals The Globals instance to apply bindings to.
     */
    private void applyCustomBindings(@NonNull Globals targetGlobals) {
        // --- Custom 'require' implementations ---
        // Standard 'require' for loading files relative to the board script's environment path
        targetGlobals.set("require", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                String path = arg.checkjstring();
                return requireFile(path); // Uses environmentPath implicitly
            }
        });

        // Special '_require' for loading library files from plugin resources
        targetGlobals.set("_require", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                String resourceName = arg.checkjstring();
                return requireLibResource(resourceName); // Uses LIB_PATH implicitly
            }
        });

        // --- Apply registered custom Java bindings (tasks, rewards, constants, etc.) ---
        // Ensure this list is populated *before* calling this method
        // (e.g., via addLuaTableBindings or passed in constructor)
        for (LuaTableBinding binding : luaBindings) {
            binding.injectBindings(targetGlobals);
        }

        // --- Load the core helper library from resources ---
        // This makes functions like tasks.kill, utils.log_info available
        requireLibResource("global"); // Assuming global.lua is the entry point
        LockoutLogger.debugLog(ChatColor.YELLOW + "Custom bindings and helper library applied.");
    }


    /**
     * Loads and executes a Lua script file from the filesystem, relative to the current environmentPath.
     * Caches the result based on the absolute path.
     *
     * @param relativeFilePath The path to the script file, relative to environmentPath.
     * @return The value returned by the executed script chunk.
     * @throws LuaError if the file cannot be found, compiled, or executed.
     */
    public LuaValue requireFile(String relativeFilePath) {
        String fullPath = Path.of(environmentPath, relativeFilePath)
                .normalize() + (relativeFilePath.endsWith(FILE_EXTENSION) ? "" : FILE_EXTENSION);

        // Check cache first
        if (requiredFilesCache.containsKey(fullPath)) {
            return requiredFilesCache.get(fullPath);
        }

        // Load and execute
        LockoutLogger.debugLog(ChatColor.YELLOW + "Loading file: " + fullPath);
        // loadfile uses the 'finder' which should be set by JseBaseLib to read files
        LuaValue chunk = scriptGlobals.loadfile(fullPath);

        LuaValue result = evalChunkAndLogErrors(chunk,
                () -> LockoutLogger.debugLog(ChatColor.GREEN + "Loaded file: " + fullPath),
                () -> LockoutLogger.warning("Failed to load file: " + fullPath));

        //LuaValue result = chunk.call();
        requiredFilesCache.put(fullPath, result); // Cache the result
        return result;
    }

    /**
     * Loads and executes a Lua script file from the plugin's internal resources (JAR).
     * Caches the result based on the resource path. Uses the internal LIB_PATH.
     *
     * @param resourceName The name of the script file within the LIB_PATH resource folder.
     * @return The value returned by the executed script chunk.
     * @throws LuaError if the resource cannot be found, read, compiled, or executed.
     */
    public LuaValue requireLibResource(String resourceName) {
        String fullResourcePath = libPath + resourceName + (resourceName.endsWith(FILE_EXTENSION) ? "" : FILE_EXTENSION);
        String cacheKey = RESOURCE_IDENTIFIER + fullResourcePath;

        // Check cache first
        if (requiredFilesCache.containsKey(cacheKey)) {
            return requiredFilesCache.get(cacheKey);
        }

        // Load from resources
        LockoutLogger.debugLog("Loading library resource: " + fullResourcePath);
        try (InputStream inputStream = plugin.getResource(fullResourcePath)) {
            if (inputStream == null) {
                throw new LuaError("Cannot load resource: " + fullResourcePath + ". Not found.");
            }
            // 'load' function from BaseLib handles InputStream
            LuaValue chunk = scriptGlobals.load(inputStream, "@" + fullResourcePath, "t", scriptGlobals);
            if (chunk.isnil()) {
                throw new LuaError("Failed to load resource: " + chunk.tojstring(2));
            }
            LuaValue result = chunk.call();
            requiredFilesCache.put(cacheKey, result); // Cache the result
            return result;
        } catch (IOException e) {
            throw new LuaError("IOException reading resource '" + fullResourcePath + "': " + e.getMessage());
        }
    }

    /**
     * Resets the Lua environment. Clears cache and reinitializes Globals
     * based on the 'sandboxed' flag set during construction.
     */
    public void reset() {
        String type = isSandboxed ? "Sandboxed" : "Standard";
        LockoutLogger.debugLog(ChatColor.YELLOW + "Resetting Lua Environment ("+ type +")...");
        requiredFilesCache.clear();

        if (isSandboxed) {
            scriptGlobals = createSandboxedGlobals();
        } else {
            scriptGlobals = JsePlatform.standardGlobals(); // Use standard for non-sandboxed (e.g., eval)
        }

        applyCustomBindings(scriptGlobals); // Apply custom functions & Lockout bindings
        LockoutLogger.debugLog(ChatColor.GREEN + "Lua Environment Reset ("+type+").");
    }

    /**
     * Loads and executes the user's init.lua from the base plugin directory (environmentPath).
     * This should generally only be called on the non-sandboxed (eval) environment instance.
     * Ensures the environment path is appropriate before calling.
     *
     * @author m-rr
     * @since 2.5.2
     */
    public void loadUserInitScript() {
        // Ensure path is set correctly before calling (e.g., to "plugins/Lockout/")
        Path initPath = Path.of(environmentPath, "init.lua");

        if (!Files.exists(initPath)) {
            LockoutLogger.debugLog("No user init.lua found at: " + initPath);
            return;
        }
        if (isSandboxed) {
            LockoutLogger.warning("Attempted to load user init.lua into a SANDBOXED Lua environment. This might fail if init.lua uses restricted libraries.");
        }

        /*try {
         LockoutLogger.debugLog("Attempting to load user " + ChatColor.GREEN + initPath);
         LuaValue chunk = scriptGlobals.loadfile(initPath.toString());
         if (chunk.isnil()) {
         throw new LuaError("Failed to load user init.lua: " + chunk.tojstring(2));
         }
         chunk.call(); // Execute in the current environment
         LockoutLogger.debugLog("Loaded user " + ChatColor.GREEN + "init.lua");
         } catch (LuaError e) {
         LockoutLogger.consoleLog(ChatColor.RED + "Error loading user init.lua in environment (" + (isSandboxed?"Sandboxed":"Standard") + "): " + e.getMessage());
         }*/

        LuaValue chunk = scriptGlobals.loadfile(initPath.toString());
        evalChunkAndLogErrors(chunk,
                () -> LockoutLogger.debugLog("Loaded user " + ChatColor.GREEN + "init.lua"),
                () -> LockoutLogger.warning("Error loading user init.lua in environment (" + (isSandboxed ? "Sandboxed" : "Standard") + ")"));
    }

    /**
     * Loads and executes a Lua file from the configured environment path.
     * Primarily used for loading the main board script file.
     * Note: This method executes the script immediately. Use requireFile for dependency loading.
     *
     * @param filePath The path to the script file, relative to the environmentPath.
     * @throws LuaError if loading or execution fails.
     */
    @Override
    public void loadFile(String filePath) {
        LockoutLogger.debugLog("Attempting to load and execute " + ChatColor.GREEN + filePath);
        String fullPath = Path.of(environmentPath, filePath)
                .normalize() + (filePath.endsWith(FILE_EXTENSION) ? "" : FILE_EXTENSION);
        LuaValue chunk = scriptGlobals.loadfile(fullPath);
        //if (chunk.isnil()) {
            //throw new LuaError("Failed to load file for execution: " + chunk.tojstring(2));
        //}
        //LuaValue result = evalChunkAndLogErrors(chunk);
        //chunk.call(); // Execute the loaded chunk
        evalChunkAndLogErrors(chunk,
                () -> LockoutLogger.debugLog("Executed file: " + fullPath),
                () -> LockoutLogger.warning("Failed to load file: " + fullPath));
    }

    /**
     * Loads and executes a Lua string within the sandboxed environment.
     * Provides temporary `_s` (sender) and `_p` (player, if applicable) globals.
     * Intended for use with the `/lockout eval` command.
     *
     * @param sender The command sender executing the string.
     * @param data The Lua code string to execute.
     * @return The value returned by the executed Lua string.
     */
    @Override
    public LuaValue loadString(CommandSender sender, String data) {
        // Temporarily set sender/player variables for the execution context
        scriptGlobals.set("_s", CoerceJavaToLua.coerce(sender));
        if (sender instanceof Player player) {
            scriptGlobals.set("_p", CoerceJavaToLua.coerce(player));
        }

        LuaValue chunk = scriptGlobals.load(data, "@eval");
        LuaValue result = evalChunkAndLogErrors(chunk,
                () -> LockoutLogger.debugLog("Evaluated: " + ChatColor.GREEN + data),
                () -> {
            LockoutLogger.error(sender.getName() + " failed to evaluate: " + data);
            LockoutLogger.log(sender, ChatColor.RED + "Failed to evaluate: " + data);
            LockoutLogger.log(sender, ChatColor.YELLOW + " Check console for details");
                });

        // Clean up temporary variables.
        scriptGlobals.set("_p", LuaValue.NIL);
        scriptGlobals.set("_s", LuaValue.NIL);

        return result;
    }

    /**
     * Adds custom Lua table bindings (like tasks, rewards, constants) to be
     * applied to the Lua environment. This should be called *before* `resetEnvironment`
     * or after the constructor if bindings are added later.
     *
     * @param bindings A list of LuaTableBinding implementations.
     */
    public void addLuaTableBindings(List<LuaTableBinding> bindings) {
        this.luaBindings.addAll(bindings);
        // If globals already exist, apply immediately? Or wait for reset/init?
        // For simplicity, let's assume they are applied during resetEnvironment/constructor.
        // If dynamic addition is needed, applyCustomBindings would need to be callable separately.
    }

    // TODO add setting to rethrow errors
    private LuaValue evalChunkAndLogErrors(LuaValue chunk, @Nullable Runnable successCallback, @Nullable Runnable failureCallback) {
        LuaValue result = LuaValue.NIL;
        try {
            if (chunk.isnil()) {
                throw new LuaError("Failed to load lua: " + chunk.tojstring(2));
            }
            result = chunk.call();
            if (successCallback != null) {
                successCallback.run();
            }
        } catch (RestrictedLuaAccessException error) {
            LockoutLogger.error(error.getMessage());
            if (failureCallback != null) {
                failureCallback.run();
            }
        } catch (LuaError error) {
            LockoutLogger.warning(error.getMessage());
            if (failureCallback != null) {
                failureCallback.run();
            }
        }

        return result;
    }

    /**
     * Sets the base filesystem path used by the custom `require` function
     * to resolve relative script paths.
     *
     * @param environmentPath The absolute or relative path to the directory containing board scripts.
     */
    public void setEnvironmentPath(String environmentPath) {
        this.environmentPath = environmentPath;
        LockoutLogger.debugLog("Lua environment path set to: " + environmentPath);
    }

    public String getEnvironmentPath() {
        return environmentPath;
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

    public boolean hasBoardDefinitionTable() {
        return scriptGlobals.get(BOARD_DEFINITION_TABLE).istable();
    }

    public LuaTable getBoardDefinitionTable() {
        return scriptGlobals.get(BOARD_DEFINITION_TABLE).checktable();
    }

}
