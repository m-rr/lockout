package stretch.lockout;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerJoinEvent; // Example event for Task
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.task.base.Task; // Assuming Task is a concrete class for testing score
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;


import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TeamManagerTest {

    private ServerMock server;
    private TestLockout testPlugin;
    private LockoutSettings settings;
    private TeamManager teamManager;

    @BeforeEach
    void setUp() {
        // Initialize MockBukkit server and load the test plugin
        server = MockBukkit.mock();
        testPlugin = MockBukkit.load(TestLockout.class);

        // Create default settings for testing
        // We load an empty config, so LockoutSettings uses its defaults or we override
        YamlConfiguration config = new YamlConfiguration();
        settings = new LockoutSettings(config); // Use defaults initially

        // Override specific settings for predictable tests
        settings.setMaxTeams(2); // Limit to 2 teams for testing maxTeams logic
        settings.setTeamSize(2); // Limit to 2 players per team
        settings.setDefaultTeams(0); // Don't create default teams automatically

        // Initialize TeamManager with our test settings
        teamManager = new TeamManager(testPlugin, settings);
    }

    @AfterEach
    void tearDown() {
        // Unload MockBukkit
        MockBukkit.unmock();
    }

    // === LockoutTeam Basic Tests ===
    // (Often tested implicitly via TeamManager, but direct tests can be useful)

    @Test
    void testTeamCreation_Properties() {
        LockoutTeam team = new LockoutTeam("TestTeam", 3);
        assertEquals("TestTeam", team.getName());
        assertEquals(3, team.getMaxSize());
        assertFalse(team.isFull());
        assertEquals(0, team.playerCount());
    }

    @Test
    void testTeamAddPlayer_CheckCountAndContains() {
        LockoutTeam team = new LockoutTeam("TeamA", 2);
        PlayerMock player1 = server.addPlayer("Player1");
        PlayerStat stat1 = new PlayerStat(player1, team);

        team.addPlayer(stat1);

        assertEquals(1, team.playerCount());
        assertTrue(team.containsPlayer(player1));
        assertTrue(team.containsPlayer(stat1));
        assertFalse(team.isFull());
    }

    @Test
    void testTeamIsFull() {
        LockoutTeam team = new LockoutTeam("TeamB", 1); // Max size 1
        PlayerMock player1 = server.addPlayer("Player1");
        PlayerStat stat1 = new PlayerStat(player1, team);
        team.addPlayer(stat1);

        assertTrue(team.isFull());
        assertEquals(1, team.playerCount());
    }

    @Test
    void testTeamRemovePlayer() {
        LockoutTeam team = new LockoutTeam("TeamC", 2);
        PlayerMock player1 = server.addPlayer("Player1");
        PlayerMock player2 = server.addPlayer("Player2");
        PlayerStat stat1 = new PlayerStat(player1, team);
        PlayerStat stat2 = new PlayerStat(player2, team);
        team.addPlayer(stat1);
        team.addPlayer(stat2);

        assertEquals(2, team.playerCount());

        team.removePlayer(player1);

        assertEquals(1, team.playerCount());
        assertFalse(team.containsPlayer(player1));
        assertTrue(team.containsPlayer(player2));

        team.removePlayer(stat2); // Test removing by PlayerStat object
        assertEquals(0, team.playerCount());
        assertFalse(team.containsPlayer(player2));
    }

    // === TeamManager Tests ===

    @Test
    void testAddTeam_Success() {
        LockoutTeam teamA = new LockoutTeam("TeamA", settings.getTeamSize());
        assertTrue(teamManager.addTeam(teamA));
        assertEquals(1, teamManager.teamCount());
        assertTrue(teamManager.isTeam("TeamA"));
        assertSame(teamManager, teamA.getTeamManager(), "TeamManager reference should be set");
    }

    @Test
    void testAddTeam_Failure_MaxTeamsReached() {
        teamManager.addTeam(new LockoutTeam("Team1", settings.getTeamSize()));
        teamManager.addTeam(new LockoutTeam("Team2", settings.getTeamSize())); // Reaches maxTeams = 2

        LockoutTeam team3 = new LockoutTeam("Team3", settings.getTeamSize());
        assertFalse(teamManager.addTeam(team3), "Should not add team beyond maxTeams limit");
        assertEquals(2, teamManager.teamCount());
        assertFalse(teamManager.isTeam("Team3"));
    }

    @Test
    void testCreateTeam_Success() {
        assertTrue(teamManager.createTeam("NewTeam"));
        assertEquals(1, teamManager.teamCount());
        assertTrue(teamManager.isTeam("NewTeam"));
    }

    @Test
    void testCreateTeam_Failure_AlreadyExists() {
        teamManager.createTeam("ExistingTeam");
        assertFalse(teamManager.createTeam("ExistingTeam"), "Should not create team with duplicate name");
        assertEquals(1, teamManager.teamCount());
    }

    @Test
    void testCreateTeam_Failure_MaxTeamsReached() {
        teamManager.createTeam("Team1");
        teamManager.createTeam("Team2"); // Reaches maxTeams = 2
        assertFalse(teamManager.createTeam("Team3"), "Should not create team beyond maxTeams limit");
        assertEquals(2, teamManager.teamCount());
    }


    @Test
    void testRemoveTeam() {
        LockoutTeam teamA = new LockoutTeam("TeamA", settings.getTeamSize());
        teamManager.addTeam(teamA);
        assertEquals(1, teamManager.teamCount());

        teamManager.removeTeam(teamA);
        assertEquals(0, teamManager.teamCount());
        assertFalse(teamManager.isTeam("TeamA"));
    }

    @Test
    void testRemoveTeamByName() {
        teamManager.addTeam(new LockoutTeam("TeamToRemove", settings.getTeamSize()));
        assertEquals(1, teamManager.teamCount());

        teamManager.removeTeamByName("TeamToRemove");
        assertEquals(0, teamManager.teamCount());
        assertFalse(teamManager.isTeam("TeamToRemove"));
    }

    @Test
    void testAddPlayerToTeam_Success() {
        PlayerMock player = server.addPlayer("TestPlayer");
        teamManager.createTeam("TargetTeam");

        assertTrue(teamManager.addPlayerToTeam(player, "TargetTeam"));
        assertTrue(teamManager.isPlayerOnTeam(player));
        assertEquals("TargetTeam", teamManager.getMappedPlayerStats().get(player).getTeam().getName());
        assertEquals(1, teamManager.getTeamByName("TargetTeam").playerCount());
    }

    @Test
    void testAddPlayerToTeam_Failure_TeamDoesNotExist() {
        PlayerMock player = server.addPlayer("TestPlayer");
        assertFalse(teamManager.addPlayerToTeam(player, "NonExistentTeam"));
        assertFalse(teamManager.isPlayerOnTeam(player));
    }

    @Test
    void testAddPlayerToTeam_Failure_TeamFull() {
        PlayerMock p1 = server.addPlayer("P1");
        PlayerMock p2 = server.addPlayer("P2");
        PlayerMock p3 = server.addPlayer("P3");
        teamManager.createTeam("FullTeam"); // teamSize = 2

        teamManager.addPlayerToTeam(p1, "FullTeam");
        teamManager.addPlayerToTeam(p2, "FullTeam");

        assertFalse(teamManager.addPlayerToTeam(p3, "FullTeam"), "Should not add player to full team");
        assertEquals(2, teamManager.getTeamByName("FullTeam").playerCount());
        assertFalse(teamManager.isPlayerOnTeam(p3));
    }

    @Test
    void testAddPlayerToTeam_Failure_PlayerAlreadyOnTeam() {
        PlayerMock player = server.addPlayer("TestPlayer");
        teamManager.createTeam("Team1");
        teamManager.createTeam("Team2");
        teamManager.addPlayerToTeam(player, "Team1");

        assertFalse(teamManager.addPlayerToTeam(player, "Team1"), "Should not re-add player to same team");
        assertFalse(teamManager.addPlayerToTeam(player, "Team2"), "Should not add player to another team if already on one");
        assertTrue(teamManager.isPlayerOnTeam(player));
        assertEquals("Team1", teamManager.getMappedPlayerStats().get(player).getTeam().getName());
    }

    @Test
    void testIsPlayerOnTeam() {
        PlayerMock p1 = server.addPlayer("P1");
        PlayerMock p2 = server.addPlayer("P2");
        teamManager.createTeam("SomeTeam");
        teamManager.addPlayerToTeam(p1, "SomeTeam");

        assertTrue(teamManager.isPlayerOnTeam(p1));
        assertFalse(teamManager.isPlayerOnTeam(p2));
    }

    @Test
    void testGetPlayerStats_And_UUIDs() {
        PlayerMock p1 = server.addPlayer("P1");
        PlayerMock p2 = server.addPlayer("P2");
        teamManager.createTeam("TeamX");
        teamManager.addPlayerToTeam(p1, "TeamX");
        teamManager.addPlayerToTeam(p2, "TeamX");

        assertEquals(2, teamManager.getPlayerStats().size());
        assertTrue(teamManager.getPlayerStats().stream().anyMatch(ps -> ps.getPlayer().equals(p1)));
        assertTrue(teamManager.getPlayerStats().stream().anyMatch(ps -> ps.getPlayer().equals(p2)));

        assertEquals(2, teamManager.getPlayerUUIDs().size());
        assertTrue(teamManager.getPlayerUUIDs().contains(p1.getUniqueId()));
        assertTrue(teamManager.getPlayerUUIDs().contains(p2.getUniqueId()));
    }

    @Test
    void testGetWinningTeam_And_IsTie() {
        // Mocking score setting via PlayerStat and Task
        PlayerMock pA1 = server.addPlayer("PlayerA1");
        PlayerMock pB1 = server.addPlayer("PlayerB1");
        PlayerMock pB2 = server.addPlayer("PlayerB2");

        LockoutTeam teamA = teamManager.getTeamByName(teamManager.createTeam("TeamA") ? "TeamA" : null);
        LockoutTeam teamB = teamManager.getTeamByName(teamManager.createTeam("TeamB") ? "TeamB" : null);

        teamManager.addPlayerToTeam(pA1, "TeamA");
        teamManager.addPlayerToTeam(pB1, "TeamB");
        teamManager.addPlayerToTeam(pB2, "TeamB");

        PlayerStat statA1 = teamManager.getMappedPlayerStats().get(pA1);
        PlayerStat statB1 = teamManager.getMappedPlayerStats().get(pB1);
        PlayerStat statB2 = teamManager.getMappedPlayerStats().get(pB2);

        // Simulate task completion
        Task task5points = new Task(PlayerJoinEvent.class, 5, "Task 5 points");
        Task task3points = new Task(PlayerJoinEvent.class, 3, "Task 3 points");

        statA1.setCompletedTask(task5points); // Team A score = 5
        statB1.setCompletedTask(task3points); // Team B score = 3
        statB2.setCompletedTask(task3points); // Team B score = 3 + 3 = 6

        assertEquals(5, teamA.getScore());
        assertEquals(6, teamB.getScore());

        assertEquals("TeamB", teamManager.getWinningTeam().getName());
        assertFalse(teamManager.isTie());

        // Make it a tie
        statA1.setCompletedTask(task3points); // Team A score = 5 + 3 = 8
        statB1.setCompletedTask(task5points); // Team B score = 3 + 3 + 5 = 11 (B1 already had 3) -> Error in logic, need fresh stat
        // Let's reset and try again simply
        teamA.getPlayerStats().clear(); // Clear for simplicity in test setup
        teamB.getPlayerStats().clear();
        statA1 = new PlayerStat(pA1, teamA);
        teamA.addPlayer(statA1);
        statB1 = new PlayerStat(pB1, teamB);
        teamB.addPlayer(statB1);
        statB2 = new PlayerStat(pB2, teamB);
        teamB.addPlayer(statB2);

        statA1.setCompletedTask(task5points); // Team A = 5
        statB1.setCompletedTask(task5points); // Team B = 5

        assertEquals(5, teamA.getScore());
        assertEquals(5, teamB.getScore());
        assertTrue(teamManager.isTie());

        // Break tie
        statB2.setCompletedTask(task3points); // Team B = 5 + 3 = 8
        assertEquals(8, teamB.getScore());
        assertFalse(teamManager.isTie());
        assertEquals("TeamB", teamManager.getWinningTeam().getName());
    }

    @Test
    void testTeamLocking() {
        PlayerMock p1 = server.addPlayer("P1");
        PlayerMock p2 = server.addPlayer("P2");

        // Before locking
        assertTrue(teamManager.createTeam("TeamPreLock"));
        assertTrue(teamManager.addPlayerToTeam(p1, "TeamPreLock"));

        // Lock teams
        teamManager.lock();
        assertTrue(teamManager.isLocked());

        // Try actions while locked
        assertFalse(teamManager.createTeam("TeamPostLock"), "Should not create team while locked");
        assertFalse(teamManager.addPlayerToTeam(p2, "TeamPreLock"), "Should not add player while locked");

        // Check that existing state is preserved
        assertTrue(teamManager.isTeam("TeamPreLock"));
        assertTrue(teamManager.isPlayerOnTeam(p1));
        assertEquals(1, teamManager.teamCount());

        // Unlock teams
        teamManager.unlock();
        assertFalse(teamManager.isLocked());

        // Try actions after unlocking
        assertTrue(teamManager.createTeam("TeamPostLock"));
        assertTrue(teamManager.addPlayerToTeam(p2, "TeamPreLock")); // Assuming team isn't full
        assertEquals(2, teamManager.teamCount());
        assertTrue(teamManager.isPlayerOnTeam(p2));
    }

    @Test
    void testGetOpposingTeams() {
        LockoutTeam teamA = new LockoutTeam("TeamA", 2);
        LockoutTeam teamB = new LockoutTeam("TeamB", 2);
        LockoutTeam teamC = new LockoutTeam("TeamC", 2);
        settings.setMaxTeams(3); // Increase for this test
        teamManager.addTeam(teamA);
        teamManager.addTeam(teamB);
        teamManager.addTeam(teamC);


        Set<LockoutTeam> opposingA = teamManager.getOpposingTeams(teamA);
        assertEquals(2, opposingA.size());
        assertTrue(opposingA.contains(teamB));
        assertTrue(opposingA.contains(teamC));
        assertFalse(opposingA.contains(teamA));

        Set<LockoutTeam> opposingB = teamManager.getOpposingTeams(teamB);
        assertEquals(2, opposingB.size());
        assertTrue(opposingB.contains(teamA));
        assertTrue(opposingB.contains(teamC));
        assertFalse(opposingB.contains(teamB));
    }

    @Test
    void testGetTeamByName_CaseInsensitive() {
        LockoutTeam teamA = new LockoutTeam("MyTeam", 2);
        teamManager.addTeam(teamA);

        assertSame(teamA, teamManager.getTeamByName("MyTeam"));
        assertSame(teamA, teamManager.getTeamByName("myteam"));
        assertSame(teamA, teamManager.getTeamByName("MYTEAM"));
    }

    @Test
    void testGetTeamByName_NotFound() {
        assertThrows(NoSuchElementException.class, () -> teamManager.getTeamByName("DoesNotExist"));
    }
}