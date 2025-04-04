package stretch.lockout.lua;

import com.google.common.collect.ImmutableList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.table.*;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.ui.bar.LockoutTimer;
import stretch.lockout.util.MessageUtil;

public class LuaEnvironment implements EvalFileHandler {
    private Globals global_table;
    private final LockoutSettings settings;
    private final Plugin plugin;
    private final String FILE_EXTENSION = ".lua";
    private String environmentPath = "plugins/Lockout/";
    private final Map<String, LuaValue> requiredFiles = new HashMap<>();
    private final List<LuaTableBinding> luaBindings = new ArrayList<>();

    public LuaEnvironment(Plugin plugin, LockoutSettings settings, List<LuaTableBinding> luaBindings) {
        this.plugin = plugin;
        this.settings = settings;
        this.global_table = JsePlatform.standardGlobals();
        this.luaBindings.addAll(luaBindings);
        initBaseTable();
    }

    public LuaEnvironment(Plugin plugin, LockoutSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.global_table = JsePlatform.standardGlobals();
        initBaseTable();
    }

    public void initUserChunk() {
        if (!Files.exists(Path.of(getEnvironmentPath(), "init.lua"))) {
            return;
        }

        MessageUtil.debugLog(settings, "Attempting to load " + ChatColor.GREEN + "init.lua");
        LuaValue userChunk = global_table.loadfile(getEnvironmentPath() + "init.lua");
        userChunk.call();
        MessageUtil.debugLog(settings, "Loaded " + ChatColor.GREEN + "init.lua");
    }

    public void initBaseTable() {
        global_table.set("require", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                String relativeFilePath = (String) CoerceLuaToJava.coerce(luaValue, String.class);
                return requireFile(relativeFilePath);
            }
        });

        luaBindings.forEach(table -> table.injectBindings(global_table));
    }

    public void addLuaTableBinding(LuaTableBinding binding) {
        luaBindings.add(binding);
    }

    public void addLuaTableBindings(List<LuaTableBinding> bindings) {
        luaBindings.addAll(bindings);
    }

    public void setEnvironmentPath(String environmentPath) {
        this.environmentPath = environmentPath;
    }

    public String getEnvironmentPath() {return environmentPath;}

    public LuaValue requireFile(String filePath) {
        if (!filePath.endsWith(FILE_EXTENSION)) {
            filePath = filePath + FILE_EXTENSION;
        }

        if (!requiredFiles.containsKey(filePath)) {
            LuaValue chunk = global_table.loadfile(getEnvironmentPath() + filePath);
            LuaValue result = chunk.call();
            requiredFiles.put(getEnvironmentPath() + filePath, result);
        }

        return requiredFiles.get(getEnvironmentPath() + filePath);
    }

    public void resetRequiredFiles() {
        requiredFiles.clear();
    }

    public void loadFile(CommandSender sender, String filePath) {
        MessageUtil.debugLog(settings, "Attempting to load " + ChatColor.GREEN + filePath);
        loadFile(filePath);
    }

    public void loadFile(String filePath) {
        MessageUtil.debugLog(settings, "Attempting to load " + ChatColor.GREEN + filePath);
        if (!filePath.endsWith(FILE_EXTENSION)) {
            filePath = filePath + FILE_EXTENSION;
        }
        LuaValue chunk = global_table.loadfile(getEnvironmentPath() + filePath);
        chunk.call();

    }

    public void loadString(CommandSender sender, String data) {
        global_table.set("_s", CoerceJavaToLua.coerce(sender));
        if (sender instanceof Player player) {
            global_table.set("_p", CoerceJavaToLua.coerce(player));
        }

        try {
            LuaValue chunk = global_table.load(data);
            chunk.call();
            String message = "Evaluated: " + ChatColor.GREEN + data;
            MessageUtil.consoleLog(message);
            if (sender instanceof Player) {
                MessageUtil.log(sender, message);
            }
        } catch (LuaError error) {
            MessageUtil.log(sender, ChatColor.RED + error.getMessage());
        }
        finally {
            global_table.set("_p", CoerceJavaToLua.coerce(false));
            global_table.set("_s", CoerceJavaToLua.coerce(false));
        }
    }

    public void resetTables() {
        global_table = JsePlatform.standardGlobals();
        initBaseTable();
    }
}
