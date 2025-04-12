package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;
import stretch.lockout.Lockout;
import stretch.lockout.board.BoardManager;
import stretch.lockout.event.executor.LockoutEventExecutor;
import stretch.lockout.event.state.PlayerStateChangeHandler;
import stretch.lockout.game.state.*;
import stretch.lockout.kit.KitHandler;
import stretch.lockout.listener.LockoutEventHandler;
import stretch.lockout.listener.OfflineRewardListener;
import stretch.lockout.listener.PlayerEventHandler;
import stretch.lockout.listener.PvpHandler;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.lua.table.*;
import stretch.lockout.reward.scheduler.RewardScheduler;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.event.IndirectTaskListener;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.task.manager.TaskManager;
import stretch.lockout.team.TeamManager;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.ui.UIManager;
import stretch.lockout.ui.inventory.InventoryInputHandler;
import stretch.lockout.ui.inventory.InventoryTaskView;
import stretch.lockout.ui.inventory.TaskSelectionView;
import stretch.lockout.util.LockoutLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LockoutContext extends GameStateManaged {
    private final TeamManager teamManager;
    private final TaskCollection mainTasks = new TaskCollection();
    private final TaskCollection tieBreaker = new TaskCollection();
    private final TaskCollection mutators = new TaskCollection();
    private final TaskManager taskManager;
    private PlayerTracker playerTracker = new PlayerTracker();
    private RewardScheduler rewardScheduler;
    private GameStateHandler gameStateHandler;
    private LockoutEventExecutor eventExecutor;
    private LuaEnvironment userLuaEnvironment;
    private BoardManager boardManager;
    private UIManager uiManager;
    private LockoutSettings settings;
    private final List<LuaTableBinding> luaBindings = new ArrayList<>();
    
    public LockoutContext(final Plugin injectedPlugin, LockoutSettings settings, BoardManager boardManager, TaskManager taskManager, TeamManager teamManager) {
        super(injectedPlugin);
        this.settings = settings;
        this.boardManager = boardManager;
        this.taskManager = taskManager;
        this.teamManager = teamManager;

        new PlayerEventHandler(this);
        new LockoutEventHandler(this);
        new InventoryInputHandler(this);
        new PvpHandler(this);
        new IndirectTaskListener(this, 20);
        new PlayerStateChangeHandler(this);
        new GameRuleEnforcer(this);

        uiManager = new UIManager(this);
        //teamManager = new TeamManager(settings);
        gameStateHandler = new DefaultStateHandler(this);
        eventExecutor = new LockoutEventExecutor(plugin, taskManager, teamManager, uiManager.getTimer(), gameStateHandler);

        rewardScheduler = new RewardScheduler(plugin, teamManager);
        new OfflineRewardListener(plugin, rewardScheduler);
        userLuaEnvironment = new LuaEnvironment(getPlugin(), settings, false);

        new KitHandler(injectedPlugin, teamManager, gameStateHandler, uiManager.getTimer(), settings);

        luaBindings.add(new LuaTaskBindings());
        luaBindings.add(new LuaRewardBindings(settings(), taskManager));
        luaBindings.add(new LuaHelperBindings(plugin, settings(), taskManager.getTasks(), getUiManager().getTimer(), getPlayerTracker()));
        luaBindings.add(new LuaClassBindings());
        luaBindings.add(new LuaPredicateBindings());

        if (settings.hasRule(LockoutGameRule.DEV)) {
            // Eval init.lua in Lockout directory. Useful for development.
            getUserLuaEnvironment().reset();
            getUserLuaEnvironment().addLuaTableBindings(luaBindings);
            getUserLuaEnvironment().loadUserInitScript();
        }

        // Make sure that our lua environments have proper bindings available
        boardManager.getLuaEnvironment().addLuaTableBindings(luaBindings);


    }

    public UIManager getUiManager() {return uiManager;}
    public GameStateHandler getGameStateHandler() {return gameStateHandler;}

    public TeamManager getTeamManager() {return teamManager;}
    public PlayerTracker getPlayerTracker() {return playerTracker;}
    public RewardScheduler getRewardScheduler() {return rewardScheduler;}
    public LuaEnvironment getUserLuaEnvironment() {return userLuaEnvironment;}
    public BoardManager getBoardManager() {return boardManager;}
    public LockoutEventExecutor getEventExecutor() {return eventExecutor;}
    public TaskManager getTaskManager() {return taskManager;}

    public void setSettings(LockoutSettings settings) {
        this.settings = settings;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    public void setBoardManager(BoardManager boardManager) {
        this.boardManager = boardManager;
    }

    public void setUserLuaEnvironment(LuaEnvironment userLuaEnvironment) {
        this.userLuaEnvironment = userLuaEnvironment;
    }

    public void setEventExecutor(LockoutEventExecutor eventExecutor) {
        this.eventExecutor = eventExecutor;
    }

    public void setGameStateHandler(GameStateHandler gameStateHandler) {
        this.gameStateHandler = gameStateHandler;
    }

    public void setRewardScheduler(RewardScheduler rewardScheduler) {
        this.rewardScheduler = rewardScheduler;
    }

    public void setPlayerTracker(PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    public void reset() {
        Bukkit.getScheduler().cancelTasks(getPlugin());
        getTeamManager().reset();
        getUiManager().reset();
        taskManager.reset();
        playerTracker = new PlayerTracker();
        rewardScheduler.cancelAll();
        LockoutLogger.debugLog("Reset lockout context");
    }

    public LockoutSettings settings() {return settings;}
    public void updateSettings(LockoutSettings settings) {this.settings = settings;}

    public InventoryTaskView getInventoryTaskView() {
        InventoryTaskView inventoryTaskView = new InventoryTaskView(settings.hasRule(LockoutGameRule.ALLOW_REWARD));
        //HashSet<TaskComponent> guiTaskComponents = new HashSet<>(getCurrentTaskCollection().getTasks());
        HashSet<TaskComponent> guiTaskComponents = new HashSet<>(taskManager.getTasks().getTasks());
        guiTaskComponents.forEach(inventoryTaskView::addTaskEntry);

        return inventoryTaskView;
    }

    public TaskSelectionView getTaskSelectionView() {
        TaskSelectionView taskSelectionView = new TaskSelectionView();
        //List<String> guiTaskLists = getPlugin().getTaskLists();
        //guiTaskLists.forEach(taskSelectionView::addTaskListEntry);

        //return taskSelectionView;
        throw new UnsupportedOperationException("Refactor");
    }

    public void gracePeriod(HumanEntity player) {
        player.setInvulnerable(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                () -> player.setInvulnerable(false), settings.getRespawnInvulnerabilityTime());
    }

    public Plugin getPlugin() {
        return this.plugin;
    }


}
