package stretch.lockout.listener; // Or your appropriate listener package

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import stretch.lockout.Lockout;
import stretch.lockout.reward.scheduler.RewardScheduler;

import java.util.Objects;

public class OfflineRewardListener implements Listener {

    private final Lockout plugin;
    private final RewardScheduler rewardScheduler;

    public OfflineRewardListener(Lockout plugin, RewardScheduler rewardScheduler) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        // Get the RewardScheduler instance from your main plugin or context
        this.rewardScheduler = Objects.requireNonNull(rewardScheduler, "RewardScheduler cannot be null");

        // Register this listener
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

