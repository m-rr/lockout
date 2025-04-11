package stretch.lockout;

// src/test/java/stretch/lockout/TestLockout.java (adjust package if needed)

import org.bukkit.configuration.file.YamlConfiguration;
import org.mockito.Mockito; // Import Mockito or your preferred mocking framework
import stretch.lockout.board.BoardManager;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.LuaEnvironment;

// Extend your main plugin class
public class TestLockout extends Lockout {

    // Fields to hold mocks and the context instance for tests to access
    public BoardManager mockBoardManager;
    public LockoutSettings testSettings;
    public LockoutContext testLockoutContext;
    public LuaEnvironment testLuaEnvironment;

    // MockBukkit calls the default constructor

    @Override
    public void onEnable() {
        // --- Create Mocks and Test Dependencies ---

        // Now you can configure your mockBoardManager before running tests
        // Example using Mockito:
        // Mockito.when(mockBoardManager.getBoards()).thenReturn(Collections.emptyList());
        // Mockito.doNothing().when(mockBoardManager).loadBoard(Mockito.anyString());
        // Create settings suitable for testing (can be customized per test later)
        this.testSettings = new LockoutSettings(new YamlConfiguration());
        // Customize testSettings if needed, e.g., disable rules that interfere

        testLuaEnvironment = new LuaEnvironment(this, testSettings, true);

        this.mockBoardManager = Mockito.mock(BoardManager.class); // Create a mock BoardManager
        Mockito.when(mockBoardManager.getLuaEnvironment()).thenReturn(testLuaEnvironment);

        // --- Create LockoutContext with Mocks ---
        // Inject the mock BoardManager and test settings
        this.testLockoutContext = new LockoutContext(this, testSettings, mockBoardManager /*, other mocks */);

        // --- Minimal Required Setup ---
        // You might need to set up the command executor if your test interacts with commands,
        // but avoid starting game states or listeners unless the test requires them.
        // getCommand("lockout").setExecutor(new LockoutCommand(this.testLockoutContext)); // Optional

        getLogger().info("TestLockout Enabled with Mock Dependencies.");
        // Do NOT call super.onEnable() - we are replacing the initialization logic.
        // Do NOT trigger the real board loading logic.
    }

    // Override getter to return the context created in this test onEnable
    @Override
    public LockoutContext getLockoutContext() {
        return testLockoutContext;
    }

    // Add getters for tests to access mocks/context easily
    public BoardManager getMockBoardManager() {
        return mockBoardManager;
    }

    public LockoutSettings getTestSettings() {
        return testSettings;
    }
}