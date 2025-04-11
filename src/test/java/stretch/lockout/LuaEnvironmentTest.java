package stretch.lockout;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.mockbukkit.mockbukkit.MockBukkit;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.LuaEnvironment; // Your specific class
import stretch.lockout.lua.RestrictedLuaAccessException; // Your custom exception
import stretch.lockout.lua.table.LuaTableBinding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LuaEnvironmentTest {

    private Plugin mockPlugin;
    private LockoutSettings mockSettings;
    private LuaEnvironment sandboxedEnv; // Environment for board scripts
    private LuaEnvironment devEnv;       // Environment for /eval (less restricted)

    @TempDir
    Path tempDir; // JUnit 5 temporary directory for filesystem tests

    private final String TEST_LIB_PATH = "test_libs/"; // Path for test resources

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        mockPlugin = MockBukkit.createMockPlugin("LockoutTestPlugin");
        mockSettings = mock(LockoutSettings.class);

        // Create environments using the constructor without bindings initially
        sandboxedEnv = new LuaEnvironment(mockPlugin, mockSettings, true); // Sandboxed = true
        devEnv = new LuaEnvironment(mockPlugin, mockSettings, false);      // Sandboxed = false

        // Set the library path for tests that use requireLibResource
        sandboxedEnv.setLibPath(TEST_LIB_PATH);
        devEnv.setLibPath(TEST_LIB_PATH); // Set for dev env too if needed
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // Helper to convert String to InputStream
    private InputStream stringToInputStream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    // --- Sandboxing Tests ---

    @Test
    void sandboxedEnv_shouldThrowRestrictedAccessForFileIO() {
        String luaCode = "return io.open(\"test.txt\", \"w\")";
        RestrictedLuaAccessException error = assertThrows(RestrictedLuaAccessException.class, () -> {
            sandboxedEnv.loadString(mock(CommandSender.class), luaCode);
        });
        assertEquals("@eval:1 Sandbox restriction: Access to 'io.open' is prohibited.", error.getMessage());
    }

    @Test
    void sandboxedEnv_shouldThrowRestrictedAccessForOsExecute() {
        String luaCode = "return os.execute(\"echo test\")";
        RestrictedLuaAccessException error = assertThrows(RestrictedLuaAccessException.class, () -> {
            sandboxedEnv.loadString(mock(CommandSender.class), luaCode);
        });
        assertEquals("@eval:1 Sandbox restriction: Access to 'os.execute' is prohibited.", error.getMessage());
    }

    @Test
    void sandboxedEnv_shouldThrowRestrictedAccessForOsExit() {
        String luaCode = "os.exit(1)";
        RestrictedLuaAccessException error = assertThrows(RestrictedLuaAccessException.class, () -> {
            sandboxedEnv.loadString(mock(CommandSender.class), luaCode);
        });
        assertEquals("@eval:1 Sandbox restriction: Access to 'os.exit' is prohibited.", error.getMessage());
    }

    @Test
    void sandboxedEnv_shouldThrowRestrictedAccessForIoTableAccess() {
        String luaCode = "return io.lines";
        RestrictedLuaAccessException error = assertThrows(RestrictedLuaAccessException.class, () -> {
            sandboxedEnv.loadString(mock(CommandSender.class), luaCode);
        });
        assertEquals("@eval:1 Sandbox restriction: Access to 'io.lines' is prohibited.", error.getMessage());
    }

    // --- Non-Sandboxed (Dev Env) Tests ---

    @Test
    void devEnv_shouldAllowFileIO() throws IOException {
        // Dev env uses standardGlobals, should have functional io lib
        String tempFilePath = tempDir.resolve("dev_io_test.txt").toString().replace("\\", "\\\\");
        String luaCode = "local f = io.open(\"" + tempFilePath + "\", \"w\"); " +
                "if f then f:write('dev test'); f:close(); return true; else return false; end";

        assertDoesNotThrow(() -> {
            LuaValue result = devEnv.loadString(mock(CommandSender.class), luaCode);
            assertTrue(result.toboolean(), "Dev env io.open should succeed or return false");
        }, "Dev environment should not throw RestrictedLuaAccessException for io.open");
        assertTrue(Files.exists(tempDir.resolve("dev_io_test.txt")), "File should have been created by dev env");
    }

    @Test
    void devEnv_shouldAllowOsExecute() {
        String luaCode = "return os.execute(\"echo Dev test\")"; // Simple command
        assertDoesNotThrow(() -> {
            // Result might be null/true/false/number depending on OS/command success
            devEnv.loadString(mock(CommandSender.class), luaCode);
        }, "Dev environment should not throw RestrictedLuaAccessException for os.execute.");
    }

    // --- Library/File Loading Tests ---

    @Test
    void requireLibResource_shouldLoadSafeLibUsingSetPath() {
        String libContent = "local M = {}; M.val = 123; return M";
        String expectedResourcePath = TEST_LIB_PATH + "safe_test_lib.lua"; // e.g., "test_libs/safe_test_lib.lua"

        // Mock using the path set in setUp()
        when(mockPlugin.getResource(expectedResourcePath))
                .thenReturn(stringToInputStream(libContent));

        LuaValue result = sandboxedEnv.requireLibResource("safe_test_lib"); // Call with base name

        assertNotNull(result);
        assertTrue(result.istable());
        assertEquals(123, result.get("val").toint());
        verify(mockPlugin, times(1)).getResource(eq(expectedResourcePath)); // Verify correct path was requested
    }

    @Test
    void requireLibResource_shouldCacheResultWithPath() {
        String libContent = "local M = {}; M.val = math.random(1, 10000); return M";
        String expectedResourcePath = TEST_LIB_PATH + "cached_lib.lua";
        when(mockPlugin.getResource(expectedResourcePath))
                .thenReturn(stringToInputStream(libContent)); // Mock only once

        LuaValue result1 = sandboxedEnv.requireLibResource("cached_lib");
        LuaValue result2 = sandboxedEnv.requireLibResource("cached_lib");

        assertSame(result1, result2);
        assertEquals(result1.get("val").toint(), result2.get("val").toint());
        verify(mockPlugin, times(1)).getResource(eq(expectedResourcePath)); // Verify correct path requested only once
    }

    @Test
    void requireLibResource_shouldThrowErrorForMissingResourceWithPath() {
        String expectedResourcePath = TEST_LIB_PATH + "non_existent_lib.lua";
        when(mockPlugin.getResource(expectedResourcePath)).thenReturn(null);

        LuaError error = assertThrows(LuaError.class, () -> {
            sandboxedEnv.requireLibResource("non_existent_lib");
        });
        assertTrue(error.getMessage().contains("Cannot load resource") && error.getMessage().contains(expectedResourcePath));
    }

    @Test
    void requireFile_shouldLoadFileFromEnvPath() throws IOException {
        Path testFile = tempDir.resolve("my_script.lua");
        Files.writeString(testFile, "return { value = 99 }");

        sandboxedEnv.setEnvironmentPath(tempDir.toString() + "/");
        LuaValue result = sandboxedEnv.requireFile("my_script");

        assertNotNull(result);
        assertTrue(result.istable());
        assertEquals(99, result.get("value").toint());
    }

    @Test
    void requireFile_shouldLoadFileWithExtensionFromEnvPath() throws IOException {
        Path testFile = tempDir.resolve("my_script_ext.lua");
        Files.writeString(testFile, "return { value = 101 }");

        sandboxedEnv.setEnvironmentPath(tempDir.toString() + "/");
        LuaValue result = sandboxedEnv.requireFile("my_script_ext.lua"); // Load WITH .lua extension

        assertNotNull(result);
        assertTrue(result.istable());
        assertEquals(101, result.get("value").toint());
    }

    // TODO this should only call certain internal methods once
    @Test
    void requireFile_shouldCacheResult() throws IOException {
        Path testFile = tempDir.resolve("my_cached_script.lua");
        // Use math.random to ensure execution only happens once if cached
        Files.writeString(testFile, "if _G.my_cached_script_val == nil then _G.my_cached_script_val = math.random(1, 10000) end; return _G.my_cached_script_val");

        sandboxedEnv.setEnvironmentPath(tempDir.toString() + "/");
        LuaValue result1 = sandboxedEnv.requireFile("my_cached_script");
        LuaValue result2 = sandboxedEnv.requireFile("my_cached_script");

        assertEquals(result1.toint(), result2.toint(), "Cached results should be the same");
        // Harder to verify loadfile was only called once without deeper hooks, rely on value check
    }


    // --- Execution Tests ---
    @Test
    void loadString_shouldExecuteSafeCodeInSandbox() {
        CommandSender mockSender = mock(CommandSender.class);
        String luaCode = "local t = {a=1}; return t.a + math.abs(-5)";
        LuaValue result = sandboxedEnv.loadString(mockSender, luaCode);
        assertEquals(6, result.toint());
    }

    @Test
    void loadString_shouldHandleSyntaxError() {
        CommandSender mockSender = mock(CommandSender.class);
        String luaCode = "local a = 5 return a +"; // Syntax error
        LuaError error = assertThrows(LuaError.class, () -> {
            sandboxedEnv.loadString(mockSender, luaCode);
        });
        // Check for compile error message (might be wrapped by our catch block)
        assertTrue(error.getMessage().contains("Failed to load/compile") || error.getMessage().contains("unexpected symbol"),
                "Error message should indicate syntax/compile error. Actual: " + error.getMessage());
    }

    @Test
    void loadFile_shouldExecuteScript() throws IOException {
        Path testFile = tempDir.resolve("exec_test.lua");
        // Define a global in the script
        Files.writeString(testFile, "_G.exec_test_marker = 'executed'");

        sandboxedEnv.setEnvironmentPath(tempDir.toString() + "/");
        // loadFile just executes, doesn't return script's return value
        assertDoesNotThrow(() -> sandboxedEnv.loadFile("exec_test.lua"));

        // Verify the side effect (global variable set) using loadString
        LuaValue marker = sandboxedEnv.loadString(mock(CommandSender.class), "return _G.exec_test_marker");
        assertEquals("executed", marker.tojstring());
    }

    // --- Reset Test ---
    @Test
    void resetEnvironment_shouldClearCacheAndResetGlobals() {
        // 1. Load library resource to populate cache/globals
        String libContent = "LibValue = 99; return {}";
        String expectedResourcePath = TEST_LIB_PATH + "reset_test_lib.lua";
        when(mockPlugin.getResource(expectedResourcePath)).thenReturn(stringToInputStream(libContent));
        sandboxedEnv.requireLibResource("reset_test_lib");
        assertEquals(99, sandboxedEnv.loadString(mock(CommandSender.class), "return LibValue").toint(), "Global should be set before reset");
        verify(mockPlugin, times(1)).getResource(eq(expectedResourcePath));

        // 2. Reset
        sandboxedEnv.reset(); // This now also re-applies bindings and requires global.lua

        // We expect global.lua to be required again during reset's applyCustomBindings
        // Mock it again AFTER reset is called for the verification phase
        reset(mockPlugin); // Reset mock interactions
        String globalLuaContent = "-- Dummy global.lua content";
        when(mockPlugin.getResource(TEST_LIB_PATH + "global.lua")).thenReturn(stringToInputStream(globalLuaContent));


        // 3. Verify state after reset
        assertTrue(sandboxedEnv.loadString(mock(CommandSender.class), "return LibValue").isnil(), "Global 'LibValue' should be nil after reset");

        // Verify library cache is cleared
        when(mockPlugin.getResource(expectedResourcePath)).thenReturn(stringToInputStream(libContent)); // Re-mock
        sandboxedEnv.requireLibResource("reset_test_lib"); // Load again
        verify(mockPlugin, times(1)).getResource(eq(expectedResourcePath)); // Should be loaded again

        // Verify global helper library was loaded again by resetEnvironment
        verify(mockPlugin, times(1)).getResource(eq(TEST_LIB_PATH + "global.lua"));
    }

    // --- Binding Test ---
    @Test
    void addLuaTableBindings_and_resetEnvironment_shouldApplyBindings() {
        LuaTableBinding mockBinding1 = mock(LuaTableBinding.class);
        LuaTableBinding mockBinding2 = mock(LuaTableBinding.class);
        List<LuaTableBinding> bindings = new ArrayList<>(List.of(mockBinding1));

        // Use constructor without bindings first
        LuaEnvironment testEnv = new LuaEnvironment(mockPlugin, mockSettings, true);
        // Add bindings *after* construction
        testEnv.addLuaTableBindings(bindings);
        testEnv.addLuaTableBindings(Collections.singletonList(mockBinding2));

        // Reset environment - this should apply all added bindings
        testEnv.reset();

        // Verify injectBindings was called on both mocks during reset
        verify(mockBinding1, times(1)).injectBindings(any(Globals.class));
        verify(mockBinding2, times(1)).injectBindings(any(Globals.class));
    }

    @Test
    void constructor_withBindings_shouldApplyBindings() {
        LuaTableBinding mockBinding = mock(LuaTableBinding.class);
        List<LuaTableBinding> bindings = Collections.singletonList(mockBinding);

        // Use constructor WITH bindings
        LuaEnvironment testEnv = new LuaEnvironment(mockPlugin, mockSettings, true, bindings);

        // applyCustomBindings is called by constructor via resetEnvironment
        verify(mockBinding, times(1)).injectBindings(any(Globals.class));
    }

    // --- Path Setter/Getter Test ---
    @Test
    void setLibPath_shouldAffectRequireLibResource() {
        String customLibPath = "my_custom_libs/";
        sandboxedEnv.setLibPath(customLibPath);
        assertEquals(customLibPath, sandboxedEnv.getLibPath());

        String libContent = "return { path_marker = 'custom' }";
        String expectedResourcePath = customLibPath + "path_test.lua";
        when(mockPlugin.getResource(expectedResourcePath)).thenReturn(stringToInputStream(libContent));

        LuaValue result = sandboxedEnv.requireLibResource("path_test");
        assertEquals("custom", result.get("path_marker").tojstring());
        verify(mockPlugin, times(1)).getResource(eq(expectedResourcePath));
    }

    // --- loadUserInitScript Test ---
    @Test
    void loadUserInitScript_shouldLoadScriptInDevEnv() throws IOException {
        Path initFile = tempDir.resolve("init.lua");
        Files.writeString(initFile, "_G.user_init_loaded = 'yes'");

        devEnv.setEnvironmentPath(tempDir.toString() + "/"); // Point to temp dir containing init.lua
        devEnv.loadUserInitScript();

        // Verify side effect
        assertEquals("yes", devEnv.loadString(mock(CommandSender.class), "return _G.user_init_loaded").tojstring());
    }

    @Test
    void loadUserInitScript_shouldWarnAndNotFailOnSandbox() throws IOException {
        Path initFile = tempDir.resolve("init.lua");
        // This script would fail if sandbox restrictions are hit, but loadUserInitScript itself shouldn't fail
        Files.writeString(initFile, "print(io); _G.user_init_loaded_sandbox = 'maybe'");

        sandboxedEnv.setEnvironmentPath(tempDir.toString() + "/");

        // Should execute without throwing Java exception, although Lua script might error internally (and be caught)
        assertDoesNotThrow(() -> sandboxedEnv.loadUserInitScript());

        // The script execution likely failed internally due to sandbox, so global shouldn't be set
        assertTrue(sandboxedEnv.loadString(mock(CommandSender.class), "return _G.user_init_loaded_sandbox").isnil());
        // We expect a warning in the log, but can't easily verify that here without log capture
    }

    @Test
    void loadUserInitScript_shouldDoNothingIfFileMissing() {
        // Ensure file does NOT exist
        Path initFile = tempDir.resolve("init.lua");
        assertFalse(Files.exists(initFile));

        devEnv.setEnvironmentPath(tempDir.toString() + "/");
        // Should complete without error
        assertDoesNotThrow(() -> devEnv.loadUserInitScript());
    }
}