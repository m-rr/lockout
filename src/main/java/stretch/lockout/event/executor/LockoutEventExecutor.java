package stretch.lockout.event.executor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.GameStateHandler;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.task.manager.TaskManager;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.ui.bar.LockoutTimer;
import stretch.lockout.util.LockoutLogger;

import java.util.Optional;

public class LockoutEventExecutor implements EventExecutor {
    private static final Listener LOCKOUT_NULL_LISTENER = new Listener() {};
    private final TaskManager taskManager;
    private final TeamManager teamManager;
    private final LockoutTimer timer;
    private final Plugin plugin;
    private final GameStateHandler gameStateHandler;

    public LockoutEventExecutor(final Plugin plugin, final TaskManager taskManager, final TeamManager teamManager, final LockoutTimer lockoutTimer, final GameStateHandler gameStateHandler) {
        this.taskManager = taskManager;
        this.teamManager = teamManager;
        this.timer = lockoutTimer;
        this.plugin = plugin;
        this.gameStateHandler = gameStateHandler;
    }

    public void register() {
        taskManager.getEventClasses()
                .forEach(clazz
                        -> Bukkit
                        .getPluginManager()
                        .registerEvent(clazz, LOCKOUT_NULL_LISTENER, EventPriority.NORMAL, this, plugin));
    }

    public void unregister() {
        HandlerList.unregisterAll(LOCKOUT_NULL_LISTENER);
        LockoutLogger.debugLog("Unregistered tasks");
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        // Ensure this event is relevant to a TaskComponent
        if (gameStateHandler.getGameState() != GameState.RUNNING
                || !taskManager.containsEventClass(event.getClass())) {
            return;
        }

        checkEvent(LockoutEventBuilder.build(event));
    }

    // TODO write tests for this
    public void checkEvent(final LockoutWrappedEvent lockoutEvent) {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (optionalPlayer.isEmpty()) {
            return;
        }
        Player player = optionalPlayer.get();

        // Ensure that both the game and player are in the proper state to complete a task
        GameState gamestate = gameStateHandler.getGameState();
        if ((gamestate != GameState.RUNNING && gamestate != GameState.TIEBREAKER)
                || player.getGameMode() == GameMode.SPECTATOR
                || !teamManager.isPlayerOnTeam(player)) {
            return;
        }

        //var currentTasks = lockout.getCurrentTaskCollection().getMappedTasks();
        //if (!currentTasks.containsKey(lockoutEvent.getEventClass())) {
        //    return;
        //}

        //var potentialTasks = currentTasks.get(lockoutEvent.getEventClass());
        var potentialTasks = taskManager.getTasks(lockoutEvent.getEventClass());
        for (TaskComponent task : potentialTasks) {
            if (!task.isCompleted() && task.doesAccomplish(lockoutEvent)) {
                PlayerStat playerStat = teamManager.getPlayerStat(player);
                //lockout.getCurrentTaskCollection().setTaskCompleted(playerStat, task);
                taskManager.setTaskCompleted(playerStat, task);
                if (task instanceof TimeCompletableTask timeTask) {
                    timeTask.setTimeCompleted(timer.elapsedTime());
                    timeTask.setLocation(player.getLocation());
                }
                TaskCompletedEvent taskCompletedEvent = new TaskCompletedEvent(task);
                Bukkit.getPluginManager().callEvent(taskCompletedEvent);
            }
        }
    }
}
