package stretch.lockout;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.GameStateHandler;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.kit.KitHandler; // The class under test
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.ui.UIManager;
import stretch.lockout.ui.bar.LockoutTimer;


import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set; // Import Set for when mocking TeamManager.getPlayerStats
import java.util.function.Consumer; // Import Consumer for doAnswer

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KitHandlerTest {

    private ServerMock server;
    private TestLockout plugin;
    private LockoutContext lockoutContext;
    private LockoutSettings settings;
    private KitHandler kitHandler; // The instance we are testing

    // Mocks for dependencies controlled via LockoutContext
    private GameStateHandler mockGameStateHandler;
    private TeamManager mockTeamManager;
    private UIManager mockUIManager;
    private LockoutTimer mockTimer;

    // Helper to check inventory for basic items
    private boolean hasCompass(PlayerMock player) {
        return player.getInventory().contains(Material.COMPASS);
    }

    private boolean hasStarterKitItems(PlayerMock player) {
        // Check for a representative item from StarterKit
        return player.getInventory().contains(Material.STONE_PICKAXE) &&
                player.getInventory().contains(Material.BREAD);
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        // Load TestLockout to get context with mocks pre-injected
        plugin = MockBukkit.load(TestLockout.class);
        lockoutContext = plugin.getLockoutContext();
        settings = lockoutContext.settings(); // Use settings from context

        // --- Get Mocks from TestLockout/LockoutContext ---
        // Assuming TestLockout or LockoutContext provides accessors for these mocks
        mockGameStateHandler = mock(GameStateHandler.class);
        mockTeamManager = mock(TeamManager.class);
        mockUIManager = mock(UIManager.class);
        mockTimer = mock(LockoutTimer.class);
        // Configure UIManager mock to return the Timer mock
        when(mockUIManager.getTimer()).thenReturn(mockTimer);

        // Inject mocks into the context *if* TestLockout didn't already do it perfectly
        // Example: lockoutContext.setGameStateHandler(mockGameStateHandler); // Requires setter or modification in TestLockout

        // --- Instantiate the class under test ---
        kitHandler = new KitHandler(plugin, mockTeamManager, mockGameStateHandler, mockTimer, settings);
        
        // Default mock behavior
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.READY); // Default state
        when(mockTeamManager.isPlayerOnTeam(any(PlayerMock.class))).thenReturn(true); // Assume players are on teams by default
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.emptyMap()); // Default to no players for safety
        when(mockTimer.hasTimeElapsed(any(Duration.class))).thenReturn(true); // Assume time condition met by default
    }


    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onJoin_ShouldApplyCompass() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        assertFalse(hasCompass(player));

        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "joined");
        server.getPluginManager().callEvent(joinEvent);

        assertTrue(hasCompass(player), "Player should receive a compass on join");
    }

    @Test
    void onStarting_ShouldApplyCompassToTeamPlayers() {
        PlayerMock player1 = server.addPlayer("P1");
        PlayerMock player2 = server.addPlayer("P2");
        player1.getInventory().clear();
        player2.getInventory().clear();
        PlayerStat stat1 = new PlayerStat(player1, null);
        PlayerStat stat2 = new PlayerStat(player2, null);

        // Mock TeamManager interactions
        when(mockTeamManager.getPlayerStats()).thenReturn(new HashSet<>(Set.of(stat1, stat2)));
        doAnswer(invocation -> {
            Consumer<Player> action = invocation.getArgument(0);
            action.accept(player1);
            action.accept(player2);
            return null;
        }).when(mockTeamManager).doToAllPlayers(any());

        assertFalse(hasCompass(player1));
        assertFalse(hasCompass(player2));

        StartGameEvent startEvent = new StartGameEvent();
        server.getPluginManager().callEvent(startEvent);

        assertTrue(hasCompass(player1), "Player1 should receive compass on start");
        assertTrue(hasCompass(player2), "Player2 should receive compass on start");

        // Verify lastRespawn map update using the new getter
        Map<Player, Long> lastRespawn = kitHandler.getLastRespawn();
        assertNotNull(lastRespawn.get(player1), "Player1 should have a lastRespawn entry");
        assertNotNull(lastRespawn.get(player2), "Player2 should have a lastRespawn entry");
        // Check if the time is recent (within a small delta)
        long now = System.currentTimeMillis();
        assertTrue(now - lastRespawn.get(player1) < 1000, "Player1 respawn time should be recent");
        assertTrue(now - lastRespawn.get(player2) < 1000, "Player2 respawn time should be recent");
    }

    @Test
    void onRespawn_ShouldApplyCompass_WhenRunning() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        // Ensure player is considered on a team
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));

        assertFalse(hasCompass(player));

        player.respawn();

        assertTrue(hasCompass(player), "Player should receive compass on respawn if game is running");
    }

    @Test
    void onRespawn_ShouldDoNothing_WhenNotRunning() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.READY); // Not RUNNING or TIEBREAKER
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));

        player.respawn();

        assertFalse(hasCompass(player));
        assertFalse(player.isInvulnerable());
        assertFalse(hasStarterKitItems(player));
    }

    @Test
    void onRespawn_ShouldDoNothing_WhenPlayerNotOnTeam() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.emptyMap()); // Player not found

        player.respawn();

        assertFalse(hasCompass(player));
        assertFalse(player.isInvulnerable());
        assertFalse(hasStarterKitItems(player));
    }


    @Test
    void onRespawn_ShouldGrantInvulnerability_WhenRuleEnabled() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        settings.gameRules().add(LockoutGameRule.RESPAWN_INVULNERABLE);
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));

        player.respawn();

        assertTrue(player.isInvulnerable(), "Player should be invulnerable");

        long expectedDelay = settings.getRespawnInvulnerabilityTime();
        // Check scheduler more reliably
        assertEquals(1, server.getScheduler().getPendingTasks().size(), "Should be one pending task");

        server.getScheduler().performTicks(expectedDelay + 1);
        assertFalse(player.isInvulnerable(), "Player should not be invulnerable after delay");
        assertEquals(0, server.getScheduler().getPendingTasks().size(), "Task should have executed");
    }

    @Test
    void onRespawn_ShouldNotGrantInvulnerability_WhenRuleDisabled() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        settings.gameRules().remove(LockoutGameRule.RESPAWN_INVULNERABLE);
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));

        player.respawn();

        assertFalse(player.isInvulnerable(), "Player should not be invulnerable");
        assertEquals(0, server.getScheduler().getPendingTasks().size(), "No task should be scheduled");
    }


    @Test
    void onRespawn_ShouldGiveStarterKit_WhenEnabledAndConditionsMet() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        settings.gameRules().add(LockoutGameRule.RESPAWN_KIT);
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));
        when(mockTimer.hasTimeElapsed(Duration.ofSeconds(settings.getRespawnKitTime() / 20))).thenReturn(true);

        // --- Explicitly simulate cooldown met using the new getter ---
        Map<Player, Long> lastRespawn = kitHandler.getLastRespawn();
        long cooldownMillis = (long) settings.getRespawnCooldownTime() * 1000;
        // Set last respawn time to be well before the cooldown period
        lastRespawn.put(player, System.currentTimeMillis() - cooldownMillis - 5000); // 5 seconds past cooldown

        assertFalse(hasStarterKitItems(player));
        player.respawn();

        assertTrue(hasStarterKitItems(player), "Player should receive starter kit items");
        // Verify lastRespawn was updated
        assertTrue(System.currentTimeMillis() - lastRespawn.get(player) < 1000, "lastRespawn time should be updated to now");
    }

    @Test
    void onRespawn_ShouldNotGiveStarterKit_WhenRuleDisabled() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        settings.gameRules().remove(LockoutGameRule.RESPAWN_KIT);
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));
        when(mockTimer.hasTimeElapsed(any(Duration.class))).thenReturn(true);


        player.respawn();

        assertFalse(hasStarterKitItems(player), "Player should not receive starter kit when rule disabled");
    }

    @Test
    void onRespawn_ShouldNotGiveStarterKit_WhenTimeConditionNotMet() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        settings.gameRules().add(LockoutGameRule.RESPAWN_KIT);
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));
        when(mockTimer.hasTimeElapsed(Duration.ofSeconds(settings.getRespawnKitTime() / 20))).thenReturn(false); // Time condition NOT met

        // Set cooldown to be met (doesn't matter here, but good practice)
        Map<Player, Long> lastRespawn = kitHandler.getLastRespawn();
        long cooldownMillis = (long) settings.getRespawnCooldownTime() * 1000;
        lastRespawn.put(player, System.currentTimeMillis() - cooldownMillis - 5000);

        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player, player.getLocation(), false);
        server.getPluginManager().callEvent(respawnEvent);

        assertFalse(hasStarterKitItems(player), "Player should not receive starter kit when time condition not met");
    }

    @Test
    void onRespawn_ShouldNotGiveStarterKit_WhenCooldownNotMet() {
        PlayerMock player = server.addPlayer();
        player.getInventory().clear();
        settings.gameRules().add(LockoutGameRule.RESPAWN_KIT);
        when(mockGameStateHandler.getGameState()).thenReturn(GameState.RUNNING);
        when(mockTeamManager.getMappedPlayerStats()).thenReturn(Collections.singletonMap(player, new PlayerStat(player, null)));
        when(mockTimer.hasTimeElapsed(any(Duration.class))).thenReturn(true); // Time condition met

        // --- Explicitly simulate cooldown NOT met using the new getter ---
        Map<Player, Long> lastRespawn = kitHandler.getLastRespawn();
        long cooldownMillis = (long) settings.getRespawnCooldownTime() * 1000;
        // Set last respawn time to be *within* the cooldown period
        lastRespawn.put(player, System.currentTimeMillis() - (cooldownMillis / 2)); // Halfway through cooldown

        assertFalse(hasStarterKitItems(player));

        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player, player.getLocation(), false);
        server.getPluginManager().callEvent(respawnEvent);

        assertFalse(hasStarterKitItems(player), "Player should not receive starter kit when cooldown not met");
    }
}
