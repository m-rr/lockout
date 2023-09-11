package stretch.lockout.game;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import stretch.lockout.Lockout;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.event.ReadyGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.kit.KitHandler;
import stretch.lockout.listener.*;
import stretch.lockout.loot.LootManager;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.reward.scheduler.RewardScheduler;
import stretch.lockout.scoreboard.bar.LockoutTimer;
import stretch.lockout.scoreboard.ScoreboardHandler;
import stretch.lockout.scoreboard.bar.PreGameBar;
import stretch.lockout.scoreboard.bar.TieBar;
import stretch.lockout.task.IndirectTaskListener;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.TaskMaterial;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.TestTeam;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.util.MessageUtil;
import stretch.lockout.view.InventoryTaskView;
import stretch.lockout.view.TaskSelectionView;

import java.time.Duration;
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
    private final LuaEnvironment luaEnvironment;
    private int maxScore;
    private GameState gameState;
    private World gameWorld;
    private long startTime;
    private final Set<GameRule> rules = new HashSet<>();
    private int countdownTime = 10;

    public void setCountdownTime(int time) {
        this.countdownTime = time;
    }

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

        setGameState(GameState.UNINIT);
    }

    public Set<GameRule> gameRules() {
        return rules;
    }

    public ScoreboardHandler getScoreboardManager() {
        return scoreboardManager;
    }
    public TaskCollection getMainTasks() {return mainTasks;}
    public TaskCollection getTieBreaker() {return tieBreaker;}

    public TaskCollection getCurrentTasks() {
        return gameState == GameState.TIEBREAKER ? tieBreaker : mainTasks;
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

    public GameState getGameState() {return this.gameState;}

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        switch (gameState) {
            case PRE -> {
                timer.setTime(Duration.ofHours(1));
                if (gameRules().contains(GameRule.AUTO_LOAD)) {
                    String taskList = Optional.ofNullable(getPlugin().getConfig()
                            .getString("autoLoadTask"))
                            .orElse("default");
                    getLuaEnvironment().loadFile(taskList);
                }

                // Add default teams
                teamManager.addDefaultTeams();

                setGameState(GameState.READY);
            }
            case READY -> {
                preGameBar.activate();
                Bukkit.getPluginManager().callEvent(new ReadyGameEvent());
            }
            case STARTING -> {
                preGameBar.deactivate();
                if (!getCurrentTasks().isTasksLoaded()) {
                    String message = ChatColor.RED + "You can not start without loading tasks!";
                    MessageUtil.consoleLog(message);
                    MessageUtil.sendAllChat(message);
                    setGameState(GameState.READY);
                    break;
                }

                // Make sure all players are on a team
                if (gameRules().contains(GameRule.FORCE_TEAM)) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (!teamManager.isPlayerOnTeam(player)) {
                            teamManager.createTeam(player.getName());
                            teamManager.addPlayerToTeam(player, player.getName());
                        }
                    });
                }

                if (gameRules().contains(GameRule.CLEAR_INV_START)) {
                    getTeamManager().clearAllPlayerEffectAndItems();
                }

                playerTracker.setAllPlayers(getTeamManager().getPlayerStats());

                Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), playerTracker, 0, 3);

                MessageUtil.sendAllChat("Game Starting!");

                getGameWorld().setTime(getStartTime());

                Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                        new CountDown(this, countdownTime,
                                getTeamManager().getPlayerStats().stream().map(PlayerStat::getPlayer).toList()), 20);
                Bukkit.getPluginManager().callEvent(new StartGameEvent());
            }
            case RUNNING -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.setInvulnerable(false);
                });
                if (gameRules().contains(GameRule.TIMER)) {
                    timer.activate();
                    timer.startTimer(getPlugin(), () -> {
                        if (getTeamManager().isTie()) {
                            setGameState(GameState.TIEBREAKER);
                        }
                        else {
                            Bukkit.getPluginManager().callEvent(new GameOverEvent(getTeamManager().getWinningTeam()));
                        }
                    });
                }
            }
            case TIEBREAKER -> {
                timer.deactivate();
                if (gameRules().contains(GameRule.TIE_BREAK) && tieBreaker.isTasksLoaded()) {
                    tieBar.activate();
                    String message = ChatColor.DARK_RED + "Tie breaker!";
                    MessageUtil.sendAllActionBar(message);
                    MessageUtil.sendAllChat(message);

                    getTeamManager().doToAllPlayers(player -> player.playSound(player, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F));
                }
                else {
                    String message = ChatColor.YELLOW + "Draw";
                    MessageUtil.sendAllActionBar(message);
                    MessageUtil.sendAllChat(message);
                    setGameState(GameState.END);
                }
            }
            case END -> {
                if (gameRules().contains(GameRule.CLEAN_INV_END)) {
                    getTeamManager().clearAllPlayerEffectAndItems();
                }

                MessageUtil.sendAllChat("Game ending.");

                Bukkit.getScheduler().cancelTasks(getPlugin());
                getScoreboardManager().resetScoreboard();
                destroyBars();
                scoreboardManager = new ScoreboardHandler();
                teamManager.destroyAllTeams();
                mainTasks = new TaskCollection();
                tieBreaker = new TaskCollection();
                playerTracker = new PlayerTracker();
                rewardScheduler = new RewardScheduler(getPlugin());
                setGameState(GameState.PRE);
            }
        }
    }

    public void destroyBars() {
        timer.deactivate();
        tieBar.deactivate();
        preGameBar.deactivate();
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
        if (getGameState() == GameState.PAUSED) {
            return;
        }

        var currentTasks = getCurrentTasks().getMappedTasks();

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
