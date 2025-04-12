package stretch.lockout.reward.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.Lockout;
import stretch.lockout.event.RewardApplyEvent;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.task.hidden.HiddenTask;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class RewardScheduler {
    private final Plugin plugin; // Keep Lockout if settings/context needed, else just Plugin
    private final TeamManager teamManager;
    // Map to track scheduled tasks for cancellation (if needed for actions)
    private final Map<RewardComponent, Queue<ScheduledTask>> scheduledActions = new HashMap<>();
    // Map to store rewards for offline players: UUID -> List of rewards to apply on login
    private final Map<UUID, List<PendingReward>> pendingOfflineRewards = new ConcurrentHashMap<>();

    public RewardScheduler(@NonNull Plugin plugin, @NonNull TeamManager teamManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.teamManager = Objects.requireNonNull(teamManager, "teamManager cannot be null");
        // TODO: Load pendingOfflineRewards from storage here (e.g., onEnable)
    }

    /**
     * Schedules the main reward application (applyReward) for a task completion.
     * Handles delays and offline players via the retired callback.
     * Also schedules any associated actions via scheduleRewardActions.
     */
    public void scheduleReward(PlayerStat playerStat, TaskComponent task) {
        RewardComponent reward = task.getReward();
        // If there's no reward, or no reward logic, nothing to do.
        if (reward == null) {
            return;
        }

        Player player = playerStat.getPlayer();
        // It's possible player logged off between task completion and this call
        if (player == null /* || !player.isOnline() - scheduler handles offline */) {
            plugin.getLogger().warning("Attempted to schedule reward for null player (UUID: " + playerStat.getPlayer().getUniqueId() + ")"); // Assuming PlayerStat has getPlayerUUID()
            // Decide if you should queue it anyway based on UUID? Let's assume not for now.
            return;
        }

        UUID playerUuid = player.getUniqueId(); // Capture UUID for the callback

        // --- Define the Retired Callback ---
        // This runs IF the player entity becomes invalid before the task executes
        Runnable retiredCallback = () -> {
            plugin.getLogger().info("Reward task retired for offline player " + playerUuid + ". Queuing reward.");
            PendingReward pending = new PendingReward(playerUuid, reward);
            // Add to the concurrent map safely
            pendingOfflineRewards.computeIfAbsent(playerUuid, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(pending);
            // TODO: Trigger save of pendingOfflineRewards to disk here for persistence
        };

        // --- Define the Core Reward Action ---
        Runnable coreRewardAction = () -> {
            // Double-check player is valid before applying reward
            Player currentPlayer = Bukkit.getPlayer(playerUuid); // Get potentially new Player object
            PlayerStat currentStat = teamManager.getPlayerStat(playerUuid); // Get potentially updated PlayerStat

            if (currentPlayer != null && currentPlayer.isOnline() && currentStat != null) {
                try {
                    reward.applyReward(currentStat); // Apply the main reward
                    // Fire event only if not hidden task and applied successfully online
                    if (!(task instanceof HiddenTask)) {
                        Bukkit.getScheduler().runTask(plugin, () -> { // Fire event synchronously
                            RewardApplyEvent rewardEvent = new RewardApplyEvent(currentStat, reward);
                            Bukkit.getPluginManager().callEvent(rewardEvent);
                        });
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error applying reward for player " + playerUuid, e);
                }
            } else {
                // This case might occur if player logs off exactly as task runs, before retired callback?
                // Queue it just in case.
                plugin.getLogger().warning("Player " + playerUuid + " became invalid during reward execution. Queuing reward.");
                PendingReward pending = new PendingReward(playerUuid, reward);
                pendingOfflineRewards.computeIfAbsent(playerUuid, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(pending);
                // TODO: Trigger save
            }
        };

        // --- Schedule the Core Reward Action ---
        long delay = reward.getDelay(); // Assuming RewardComponent has getDelay() returning ticks
        if (delay > 0L) {
            player.getScheduler().runDelayed(plugin, scheduledTask -> coreRewardAction.run(), retiredCallback, delay);
        } else {
            player.getScheduler().run(plugin, scheduledTask -> coreRewardAction.run(), retiredCallback);
        }

        // --- Schedule Associated Actions (if any) ---
        // These might need separate offline handling if they are critical,
        // but often they are secondary effects. The current scheduleRewardActions
        // already uses the retired callback, but doesn't queue the *action* itself.
        // For simplicity, we'll assume only the main reward needs robust offline handling for now.
        // If specific actions MUST run, they should arguably be part of the applyReward logic.
        var actions = reward.getActions();
        if (!actions.isEmpty()) {
            // This uses the *old* retired callback logic which just logs.
            // Consider if these actions also need to be queued.
            scheduleRewardActions(playerStat, reward);
        }
    }

    /**
     * Schedules secondary actions associated with a reward.
     * Uses EntityScheduler and includes a basic retired callback.
     * NOTE: This version does NOT queue the action itself if the player logs off.
     */
    public void scheduleRewardActions(PlayerStat playerStat, RewardComponent reward) {
        // ... (validation as before) ...
        Player player = playerStat.getPlayer();
        if (player == null) return; // Cannot schedule on null player
        UUID playerUuid = player.getUniqueId(); // Capture UUID

        var actionsToSchedule = reward.getActions(); // Assuming Map<Consumer<Player>, Long>
        if (actionsToSchedule.isEmpty()) return;

        scheduledActions.computeIfAbsent(reward, k -> new LinkedList<>());
        Queue<ScheduledTask> taskQueue = scheduledActions.get(reward);

        // --- Define Retired Callback for Actions ---
        // This simple version just logs, doesn't queue the action.
        Runnable retiredActionCallback = () -> {
            plugin.getLogger().info("Secondary reward action task retired for offline player " + playerUuid + ". Action NOT queued.");
            // If queuing is needed, capture the 'consumer' and add to pending map
        };

        actionsToSchedule.forEach((consumer, delayTicks) -> {
            ScheduledTask scheduledTask;
            long effectiveDelay = Math.max(0, delayTicks);

            // Define the action to run
            Runnable actionRunnable = () -> {
                Player currentPlayer = Bukkit.getPlayer(playerUuid); // Re-fetch player
                if (currentPlayer != null && currentPlayer.isOnline()) {
                    try {
                        consumer.accept(currentPlayer); // Execute the action
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error running scheduled reward action for player " + playerUuid, e);
                    }
                } else {
                    // Player logged off just as task ran? Log it.
                    plugin.getLogger().warning("Player " + playerUuid + " invalid during scheduled action execution.");
                    // Action is lost in this implementation.
                }
            };

            // Schedule using the player's EntityScheduler
            if (effectiveDelay < 1L) {
                scheduledTask = player.getScheduler().run(plugin, task -> actionRunnable.run(), retiredActionCallback);
            } else {
                scheduledTask = player.getScheduler().runDelayed(plugin, task -> actionRunnable.run(), retiredActionCallback, effectiveDelay);
            }
            taskQueue.offer(scheduledTask);
        });
    }

    /**
     * Called when a player joins, attempts to apply any pending rewards.
     */
    public void applyPendingRewards(Player player) {
        UUID playerUuid = player.getUniqueId();
        List<PendingReward> pendingList = pendingOfflineRewards.remove(playerUuid); // Remove and get

        if (pendingList != null && !pendingList.isEmpty()) {
            plugin.getLogger().info("Applying " + pendingList.size() + " pending rewards for player " + player.getName());
            PlayerStat currentStat = teamManager.getPlayerStat(playerUuid);

            if (currentStat == null) {
                plugin.getLogger().warning("Cannot apply pending rewards for " + player.getName() + ": PlayerStat not found (maybe left team?).");
                // Optionally re-queue or discard based on game rules
                // pendingOfflineRewards.put(playerUuid, pendingList); // Re-queue example
                return; // Stop processing if stat is missing
            }

            // Use synchronized block when iterating/modifying the list if using ArrayList
            synchronized (pendingList) { // Necessary if using synchronizedList wrapper
                for (PendingReward pending : pendingList) {
                    try {
                        RewardComponent reward = pending.reward();
                        plugin.getLogger().info("Applying pending reward: " + reward.getDescription() + " to " + player.getName());
                        reward.applyReward(currentStat); // Apply the reward

                        // Fire event synchronously
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            RewardApplyEvent rewardEvent = new RewardApplyEvent(currentStat, reward);
                            Bukkit.getPluginManager().callEvent(rewardEvent);
                        });

                        // Schedule associated actions if needed (using current logic which doesn't queue actions)
                        if (!reward.getActions().isEmpty()) {
                            scheduleRewardActions(currentStat, reward);
                        }

                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error applying PENDING reward for player " + playerUuid, e);
                    }
                }
                // List is already removed from the main map, no need to clear it from map again
            } // End synchronized block
            // TODO: Trigger save of pendingOfflineRewards map (as it has changed)
        }
    }


    public void cancelRewardActions(RewardComponent reward) {
        // ... (implementation remains the same, cancels ScheduledTask) ...
        if (scheduledActions.containsKey(reward)) {
            Queue<ScheduledTask> taskQueue = scheduledActions.get(reward);
            ScheduledTask task;
            // Poll until empty, cancelling non-cancelled tasks
            while ((task = taskQueue.poll()) != null) {
                if (!task.isCancelled()) {
                    task.cancel();
                }
            }
            scheduledActions.remove(reward);
        }
    }

    public void cancelAll() {
        new HashSet<>(scheduledActions.keySet()).forEach(this::cancelRewardActions);
        scheduledActions.clear();
        // Consider if pendingOfflineRewards should also be cleared on cancelAll/plugin disable?
        // pendingOfflineRewards.clear(); // Optional: Clear offline queue on disable/reset
        // TODO: Save pendingOfflineRewards map on disable
    }

    // --- TODO: Implement Persistence ---
    public void saveOfflineRewards() {
        plugin.getLogger().info("Saving offline rewards (Not Implemented)...");
        // Implementation: Serialize pendingOfflineRewards map to a file (JSON/YAML) or database.
    }

    public void loadOfflineRewards() {
        plugin.getLogger().info("Loading offline rewards (Not Implemented)...");
        // Implementation: Load data from file/database into pendingOfflineRewards map.
        // Ensure data structures are compatible (e.g., deserialize RewardComponent correctly).
        // This might require RewardComponents to be serializable or identifiable by a key.
    }
}

