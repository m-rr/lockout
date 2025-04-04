package stretch.lockout.event.executor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.entity.Player;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.state.GameState;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.util.LockoutLogger;

import java.util.Optional;

public class LockoutEventExecutor implements EventExecutor {
    private static final Listener LOCKOUT_NULL_LISTENER = new Listener() {};
    private final LockoutContext lockout;

    public LockoutEventExecutor(final LockoutContext lockout) {
        this.lockout = lockout;
    }

    // TODO support tiebreaker
    public void register() {
        lockout.getMainTasks().getEventClasses()
                .forEach(clazz -> Bukkit.getPluginManager()
                        .registerEvent(clazz,
                                LOCKOUT_NULL_LISTENER,
                                EventPriority.NORMAL,
                                this,
                                lockout.getPlugin()));
        LockoutLogger.debugLog(lockout.settings(), "Registered tasks");
    }

    public void unregister() {
        HandlerList.unregisterAll(LOCKOUT_NULL_LISTENER);
        LockoutLogger.debugLog(lockout.settings(), "Unregistered tasks");
    }

    @Override
    public void execute(Listener listener, Event event) {
        if (lockout.getGameStateHandler().getGameState() != GameState.RUNNING
                || !lockout.getCurrentTaskCollection().getMappedTasks().containsKey(event.getClass())) {
            return;
        }

        checkEvent(LockoutEventBuilder.build(event));
    }

    public void checkEvent(final LockoutWrappedEvent lockoutEvent) {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (optionalPlayer.isEmpty()) {
            return;
        }
        Player player = optionalPlayer.get();

        GameState gamestate = lockout.getGameStateHandler().getGameState();
        if ((gamestate != GameState.RUNNING && gamestate != GameState.TIEBREAKER)
                || player.getGameMode() == GameMode.SPECTATOR
                || !lockout.getTeamManager().isPlayerOnTeam(player)) {
            return;
        }

        var currentTasks = lockout.getCurrentTaskCollection().getMappedTasks();
        if (!currentTasks.containsKey(lockoutEvent.getEventClass())) {
            return;
        }

        var potentialTasks = currentTasks.get(lockoutEvent.getEventClass());
        for (TaskComponent task : potentialTasks) {
            if (!task.isCompleted() && task.doesAccomplish(lockoutEvent)) {
                PlayerStat playerStat = lockout.getTeamManager().getMappedPlayerStats().get(player);
                lockout.getCurrentTaskCollection().setTaskCompleted(playerStat, task);
                if (task instanceof TimeCompletableTask timeTask) {
                    timeTask.setTimeCompleted(lockout.getUiManager().getTimer().elapsedTime());
                    timeTask.setLocation(player.getLocation());
                }
                TaskCompletedEvent taskCompletedEvent = new TaskCompletedEvent(task);
                Bukkit.getPluginManager().callEvent(taskCompletedEvent);
            }
        }
    }
}
