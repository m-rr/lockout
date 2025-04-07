package stretch.lockout.reward.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import stretch.lockout.Lockout;
import stretch.lockout.event.RewardApplyEvent;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.task.HiddenTask;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.team.player.PlayerStat;

import java.util.*;

public class RewardScheduler {
    private final Lockout plugin;
    private final Map<RewardComponent, Queue<ScheduledTask>> scheduledActions = new HashMap<>();
    private final Runnable playerRetiredCallback = () -> {
        throw new UnsupportedOperationException("Not implemented yet");
    };

    public RewardScheduler(Lockout plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    public void scheduleReward(PlayerStat playerStat, TaskComponent task) {
        var reward = task.getReward();
        var player = playerStat.getPlayer();

        if (reward.getDelay() > 0L) {
            player.getScheduler().runDelayed(plugin, scheduledTask -> {
                reward.applyReward(playerStat);
                if (!(task instanceof HiddenTask)) {
                    var rewardEvent = new RewardApplyEvent(playerStat, reward);
                    Bukkit.getPluginManager().callEvent(rewardEvent);
                }
            }, playerRetiredCallback, reward.getDelay());
        }
        else {
            player.getScheduler().run(plugin, scheduledTask -> {
                reward.applyReward(playerStat);
                if (!(task instanceof HiddenTask)) {
                    var rewardEvent = new RewardApplyEvent(playerStat, reward);
                    Bukkit.getPluginManager().callEvent(rewardEvent);
                }
            }, playerRetiredCallback);
        }


        // Perform any actions associated with this reward
        var actions = reward.getActions();
        if (!actions.isEmpty()) {
            scheduleRewardActions(playerStat, reward);
        }
    }

    public void scheduleRewardActions(PlayerStat playerStat, RewardComponent reward) {
        Objects.requireNonNull(reward, "reward cannot be null");
        Objects.requireNonNull(playerStat, "playerStat cannot be null");

        Player player = playerStat.getPlayer();
        if (player == null || !player.isOnline()) {
            // Don't schedule tasks for offline players
            // Optionally log this:
            // MessageUtil.debugLog(plugin, "Skipping reward schedule for offline/null player: " + (player == null ? "null" : player.getName()));
            return;
        }

        var actionsToSchedule = reward.getActions();
        if (actionsToSchedule.isEmpty()) {
            return; // Nothing to schedule
        }

        // Ensure a queue exists for this reward component instance
        scheduledActions.computeIfAbsent(reward, k -> new LinkedList<>());
        Queue<ScheduledTask> taskQueue = scheduledActions.get(reward);

        actionsToSchedule.forEach((runnable, delayTicks) -> {
            ScheduledTask scheduledTask;
            // Paper scheduler uses ticks for delay, same as Bukkit
            long effectiveDelay = Math.max(0, delayTicks); // Ensure delay isn't negative

            // Schedule using the player's EntityScheduler
            if (effectiveDelay < 1L) {
                // Schedule to run as soon as possible on the entity's region thread
                scheduledTask = player.getScheduler().run(plugin, task -> runnable.accept(player), playerRetiredCallback);
            } else {
                // Schedule with a delay
                scheduledTask = player.getScheduler().runDelayed(plugin, task -> runnable.accept(player), playerRetiredCallback, effectiveDelay);
            }
            // Add the Paper ScheduledTask to our tracking queue
            taskQueue.offer(scheduledTask);
        });
    }

    public void cancelRewardActions(RewardComponent reward) {
        if (scheduledActions.containsKey(reward)) {
            Queue<ScheduledTask> taskQueue = scheduledActions.get(reward);
            ScheduledTask task;
            while ((task = taskQueue.poll()) != null) {
                if (!task.isCancelled()) {
                    task.cancel();
                }
            }
            // Remove the entry once all tasks associated with it are processed/cancelled
            scheduledActions.remove(reward);
        }
    }

    public void cancelAll() {
        new HashSet<>(scheduledActions.keySet()).forEach(this::cancelRewardActions);
        scheduledActions.clear();
    }
}
