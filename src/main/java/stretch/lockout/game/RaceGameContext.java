package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import stretch.lockout.Lockout;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.game.state.DefaultStateHandler;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.GameStateHandler;
import stretch.lockout.kit.KitHandler;
import stretch.lockout.listener.*;
import stretch.lockout.loot.LootManager;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.reward.scheduler.RewardScheduler;
import stretch.lockout.scoreboard.ScoreboardHandler;
import stretch.lockout.scoreboard.bar.LockoutTimer;
import stretch.lockout.scoreboard.bar.PreGameBar;
import stretch.lockout.scoreboard.bar.TieBar;
import stretch.lockout.task.IndirectTaskListener;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.view.InventoryTaskView;
import stretch.lockout.view.TaskSelectionView;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class RaceGameContext {
    final private Lockout plugin;
    private TeamManager teamManager = new TeamManager();
    private TaskCollection mainTasks = new TaskCollection();
    //private TaskManager taskManager = new TaskManager();
    private TaskCollection tieBreaker = new TaskCollection();

    private final LootManager lootManager = new LootManager();
    private ScoreboardHandler scoreboardManager = new ScoreboardHandler();
    private PlayerTracker playerTracker = new PlayerTracker();
    private final LockoutTimer timer = new LockoutTimer();
    private final TieBar tieBar = new TieBar();
    private final PreGameBar preGameBar = new PreGameBar();
    private RewardScheduler rewardScheduler;
    private GameStateHandler gameStateHandler;
    private final LuaEnvironment luaEnvironment;
    private int maxScore;
    private World gameWorld;
    private long startTime;
    private final Set<GameRule> rules = new HashSet<>();
    private int countdownTime = 10;

    public RaceGameContext(final Lockout injectedPlugin) {
        this.plugin = injectedPlugin;
        this.maxScore = 0;
        new PlayerEventHandler(this);
        new EntityEventHandler(this);
        new BlockEventHandler(this);
        new TaskRaceEventHandler(this);
        new InventoryEventHandler(this);
        new PvpHandler(this);
        new IndirectTaskListener(this, 20);
        new KitHandler(this);

        rewardScheduler = new RewardScheduler(getPlugin());
        luaEnvironment = new LuaEnvironment(this);
        lootManager.setWorld(Bukkit.getWorld("world"));

        gameStateHandler = new DefaultStateHandler(this);
    }

    public Set<GameRule> gameRules() {
        return rules;
    }

    public ScoreboardHandler getScoreboardManager() {
        return scoreboardManager;
    }
    public TaskCollection getMainTasks() {return mainTasks;}
    public TaskCollection getTieBreaker() {return tieBreaker;}
    public GameStateHandler getGameStateHandler() {return gameStateHandler;}
    public void setGameStateHandler(GameStateHandler gameStateHandler) {
        this.gameStateHandler = gameStateHandler;
    }
    public void setCountdownTime(int time) {
        this.countdownTime = time;
    }

    public int getCountdownTime() {return countdownTime;}

    public TaskCollection getCurrentTasks() {
        return getGameStateHandler().getGameState() == GameState.TIEBREAKER ? tieBreaker : mainTasks;
    }
    public LockoutTimer getTimer() {return timer;}
    public PreGameBar getPreGameBar() {return preGameBar;}
    public TieBar getTieBar() {return tieBar;}

    public LootManager getLootManager() {return lootManager;}

    public TeamManager getTeamManager() {return teamManager;}
    //public TaskManager getTaskManager() {return taskManager;}

    public PlayerTracker getPlayerTracker() {return playerTracker;}

    public RewardScheduler getRewardScheduler() {return rewardScheduler;}

    public LuaEnvironment getLuaEnvironment() {return luaEnvironment;}

    public void destroyBars() {
        timer.deactivate();
        tieBar.deactivate();
        preGameBar.deactivate();
    }


    public void reset() {
        Bukkit.getScheduler().cancelTasks(getPlugin());
        getScoreboardManager().resetScoreboard();
        destroyBars();
        getTeamManager().destroyAllTeams();
        scoreboardManager = new ScoreboardHandler();
        mainTasks = new TaskCollection();
        tieBreaker = new TaskCollection();
        playerTracker = new PlayerTracker();
        rewardScheduler = new RewardScheduler(getPlugin());
    }

    public int getMaxScore() {
        return maxScore;
    }
    public World getGameWorld() {return gameWorld;}
    public long getStartTime() {return startTime;}
    public void setStartTime(long startTime) {this.startTime = startTime;}

    public void setMaxScore(final int score) {
        maxScore = score;
    }

    public void setGameWorld(final String worldName) {
        Optional<World> world = Optional.ofNullable(Bukkit.getWorld(worldName));
        world.ifPresentOrElse(w -> gameWorld = w,
                () -> getPlugin().getLogger().log(Level.WARNING,
                        "Could not load world: " + worldName));
    }

    public long getRewardPotionTicks() {
        return 144000L;
    }

    public InventoryTaskView getInventoryTaskView() {
        var inventoryTaskView = new InventoryTaskView(gameRules().contains(GameRule.ALLOW_REWARD));
        HashSet<TaskComponent> guiTaskComponents = new HashSet<>(getCurrentTasks().getTasks());
        guiTaskComponents.forEach(inventoryTaskView::addTaskEntry);

        return inventoryTaskView;
    }

    public TaskSelectionView getTaskSelectionView() {
        var taskSelectionView = new TaskSelectionView();
        List<String> guiTaskLists = getPlugin().getTaskLists();
        guiTaskLists.forEach(taskSelectionView::addTaskListEntry);

        return taskSelectionView;
    }

  // Events must be player completable
    public void checkTask(Player player, Event event) {
        if (getGameStateHandler().getGameState() == GameState.PAUSED) {
            return;
        }

        var currentTasks = getCurrentTasks().getMappedTasks();
        GameState gameState = getGameStateHandler().getGameState();

        if ((gameState == GameState.RUNNING || gameState == GameState.TIEBREAKER) && player.getGameMode() != GameMode.SPECTATOR
                && currentTasks.containsKey(event.getClass()) && teamManager.isPlayerOnTeam(player)) {

            var potentialTasks = currentTasks.get(event.getClass());
            for (var task : potentialTasks) {
                if (!task.isCompleted() && task.doesAccomplish(player, event)) {
                    PlayerStat playerStat = teamManager.getMappedPlayerStats().get(player);
                    getCurrentTasks().setTaskCompleted(playerStat, task);

                    TaskCompletedEvent taskEvent = new TaskCompletedEvent(task);
                    Bukkit.getPluginManager().callEvent(taskEvent);
                }
            }
        }
    }

    public void gracePeriod(HumanEntity player) {
        player.setInvulnerable(true);
        final int GRACE_INVULNERABLE_TIME = 140;
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                () -> player.setInvulnerable(false), GRACE_INVULNERABLE_TIME);
    }

    public Lockout getPlugin() {
        return this.plugin;
    }
}
