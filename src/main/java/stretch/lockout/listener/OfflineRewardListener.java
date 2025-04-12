package stretch.lockout.listener; // Or your appropriate listener package

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.Lockout;
import stretch.lockout.reward.scheduler.RewardScheduler;

import java.util.Objects;

/**
 * Listens for {@link PlayerJoinEvent} and ensures that any pending rewards are applied to
 * the player that they may have missed while disconnected.
 *
 * @author m-rr
 * @version @projectVersion@
 * @see RewardScheduler
 * @see stretch.lockout.reward.scheduler.PendingReward
 * @see stretch.lockout.reward.api.RewardComponent
 * @since 2.5.1
 * */
public class OfflineRewardListener implements Listener {

    private final Plugin plugin;
    private final RewardScheduler rewardScheduler;

    public OfflineRewardListener(@NonNull Plugin plugin, @NonNull RewardScheduler rewardScheduler) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.rewardScheduler = Objects.requireNonNull(rewardScheduler, "RewardScheduler cannot be null");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Use Bukkit scheduler to run slightly later, ensuring player is fully loaded
        // and potentially has their PlayerStat available in TeamManager.
        plugin.getServer().getScheduler().runTaskLater(plugin, scheduledTask -> {
            if (player.isOnline()) { // Check if player didn't immediately leave again
                rewardScheduler.applyPendingRewards(player);
            }
        }, 10L); // Delay slightly
    }
}

