package stretch.lockout.reward.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import stretch.lockout.Lockout;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.reward.function.RewardRunnable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class RewardScheduler {
    private final Lockout plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Map<RewardComponent, Queue<BukkitTask>> scheduledActions = new HashMap<>();
    public RewardScheduler(Lockout plugin) {
        this.plugin = plugin;
    }

    private BukkitTask runTask(Runnable action) {
        return scheduler.runTask(plugin, action);
    }

    public void scheduleRewardAction(RewardRunnable action) {
        RewardComponent reward = action.getReward();
        if (!scheduledActions.containsKey(reward)) {
            scheduledActions.put(reward, new LinkedList<>());
        }
        scheduledActions.get(reward).offer(runTask(action));
    }

    public boolean isCurrentlyRunning(RewardComponent reward) {
        return scheduledActions.containsKey(reward) &&
                scheduledActions.get(reward).stream()
                .mapToInt(BukkitTask::getTaskId)
                .anyMatch(scheduler::isCurrentlyRunning);
    }

    public void cancelRewardActions(RewardComponent reward) {
        if (scheduledActions.containsKey(reward)) {
            scheduledActions.get(reward).stream()
                    .mapToInt(BukkitTask::getTaskId)
                    .forEach(scheduler::cancelTask);
            scheduledActions.put(reward, new LinkedList<>());
        }
    }
}
