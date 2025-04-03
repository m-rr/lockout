package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import stretch.lockout.Lockout;
import stretch.lockout.board.BoardManager;
import stretch.lockout.event.executor.LockoutEventExecutor;
import stretch.lockout.event.state.PlayerStateChangeHandler;
import stretch.lockout.game.state.DefaultStateHandler;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.GameStateHandler;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.kit.KitHandler;
import stretch.lockout.listener.*;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.reward.scheduler.RewardScheduler;
import stretch.lockout.ui.UIManager;
import stretch.lockout.ui.inventory.InventoryInputHandler;
import stretch.lockout.task.IndirectTaskListener;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.team.TeamManager;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.ui.inventory.InventoryTaskView;
import stretch.lockout.ui.inventory.TaskSelectionView;

import java.util.*;

public class LockoutContext {
    final private Lockout plugin;
    private final TeamManager teamManager;
    private TaskCollection mainTasks = new TaskCollection();
    private TaskCollection tieBreaker = new TaskCollection();
    private PlayerTracker playerTracker = new PlayerTracker();
    private RewardScheduler rewardScheduler;
    private final GameStateHandler gameStateHandler;
    private final LockoutEventExecutor eventExecutor;
    private final LuaEnvironment userLuaEnvironment;
    private final BoardManager boardManager;
    private final UIManager uiManager;
    private LockoutSettings settings;
    
    public LockoutContext(final Lockout injectedPlugin, LockoutSettings settings) {
        this.plugin = injectedPlugin;
        this.settings = settings;

        new PlayerEventHandler(this);
        new TaskRaceEventHandler(this);
        new InventoryInputHandler(this);
        new PvpHandler(this);
        new IndirectTaskListener(this, 20);
        new KitHandler(this);
        new PlayerStateChangeHandler(this);
        new GameRuleEnforcer(this);

        uiManager = new UIManager(this);
        eventExecutor = new LockoutEventExecutor(this);
        teamManager = new TeamManager(settings);
        rewardScheduler = new RewardScheduler(getPlugin());
        userLuaEnvironment = new LuaEnvironment(this);
        boardManager = new BoardManager(this);

        gameStateHandler = new DefaultStateHandler(this);
    }

    public UIManager getUiManager() {return uiManager;}
    public TaskCollection getMainTasks() {return mainTasks;}
    public TaskCollection getTieBreaker() {return tieBreaker;}
    public GameStateHandler getGameStateHandler() {return gameStateHandler;}

    public TaskCollection getCurrentTaskCollection() {
        return getGameStateHandler().getGameState() == GameState.TIEBREAKER ? tieBreaker : mainTasks;
    }

    public TeamManager getTeamManager() {return teamManager;}
    public PlayerTracker getPlayerTracker() {return playerTracker;}
    public RewardScheduler getRewardScheduler() {return rewardScheduler;}
    public LuaEnvironment getUserLuaEnvironment() {return userLuaEnvironment;}
    public BoardManager getBoardManager() {return boardManager;}
    public LockoutEventExecutor getEventExecutor() {return eventExecutor;}

    public void reset() {
        Bukkit.getScheduler().cancelTasks(getPlugin());
        getTeamManager().destroyAllTeams();
        getTeamManager().unlock();
        getUiManager().reset();
        mainTasks = new TaskCollection();
        tieBreaker = new TaskCollection();
        playerTracker = new PlayerTracker();
        rewardScheduler = new RewardScheduler(getPlugin());
    }

    public LockoutSettings settings() {return settings;}
    public void updateSettings(LockoutSettings settings) {this.settings = settings;}

    public InventoryTaskView getInventoryTaskView() {
        InventoryTaskView inventoryTaskView = new InventoryTaskView(settings.hasRule(LockoutGameRule.ALLOW_REWARD));
        HashSet<TaskComponent> guiTaskComponents = new HashSet<>(getCurrentTaskCollection().getTasks());
        guiTaskComponents.forEach(inventoryTaskView::addTaskEntry);

        return inventoryTaskView;
    }

    public TaskSelectionView getTaskSelectionView() {
        TaskSelectionView taskSelectionView = new TaskSelectionView();
        List<String> guiTaskLists = getPlugin().getTaskLists();
        guiTaskLists.forEach(taskSelectionView::addTaskListEntry);

        return taskSelectionView;
    }

    public void gracePeriod(HumanEntity player) {
        player.setInvulnerable(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                () -> player.setInvulnerable(false), settings.getRespawnInvulnerabilityTime());
    }

    public Lockout getPlugin() {
        return this.plugin;
    }


}
