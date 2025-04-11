package stretch.lockout.task.hidden;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.team.player.PlayerStat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class HiddenTask implements TaskComponent {
    private final TaskComponent taskComponent;
    private final Set<UUID> whitelistPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ScheduledTask> scheduledUnsubscribeTasks = new ConcurrentHashMap<>();
    private long activeSubscriptionTimeSeconds = 0L;

    public HiddenTask(TaskComponent taskComponent) {
        this.taskComponent = taskComponent;
    }

    /**
     * If no activeSubscriptionTimeSeconds has been set, then players are subscribed indefinitely.
     * Otherwise, they are unsubscribed after the desired time.
     * Note that any player subscribed before activeSubscriptionTimeSeconds has been set will
     * be subscribed indefinitely.
     * */

    public HiddenTask subscribe(UUID uuid) {
        whitelistPlayers.add(uuid);
        removeUnsubscribeTasks(uuid);
        if (activeSubscriptionTimeSeconds > 0L) {
            unsubscribeAfter(uuid, activeSubscriptionTimeSeconds);
        }
        return this;
    }

    private void unsubscribeAfter(UUID uuid, long seconds) {
        if (whitelistPlayers.contains(uuid)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Lockout");
            assert plugin != null;

            ScheduledTask unsubTask = Bukkit.getAsyncScheduler().runDelayed(plugin,
                    scheduledTask -> this.unsubscribe(uuid), seconds, TimeUnit.SECONDS);

            scheduledUnsubscribeTasks.put(uuid, unsubTask);
        }
    }

    public void unsubscribe(UUID uuid) {
        whitelistPlayers.remove(uuid);
        // Make sure that we do not try to unsubscribe them twice
        removeUnsubscribeTasks(uuid);
    }

    private void removeUnsubscribeTasks(UUID uuid) {
        if (scheduledUnsubscribeTasks.containsKey(uuid)) {
            ScheduledTask unsubTask = scheduledUnsubscribeTasks.remove(uuid);
            unsubTask.cancel();
        }
    }

    public long getActiveSubscriptionTimeSeconds() {return this.activeSubscriptionTimeSeconds;}

    public HiddenTask setActiveSubscriptionTimeSeconds(long seconds) {
        this.activeSubscriptionTimeSeconds = seconds;
        return this;
    }

    @Override
    public HashSet<Class<? extends Event>> getEventClasses() {
        return taskComponent.getEventClasses();
    }

    @Override
    public PlayerStat getScoredPlayer() {
        return taskComponent.getScoredPlayer();
    }

    @Override
    public void setCompletedBy(PlayerStat scoringPlayer) {
        taskComponent.setCompletedBy(scoringPlayer);
    }

    @Override
    public boolean isCompleted() {
        // Never completed
        return false;
    }

    @Override
    public ItemStack getGuiItemStack() {
        return taskComponent.getGuiItemStack();
    }

    @Override
    public TaskComponent setGuiItemStack(ItemStack itemStack) {
        taskComponent.setGuiItemStack(itemStack);
        return this;
    }

    @Override
    public Material getDisplay() {
        return Material.AIR;
    }

    @Override
    public TaskComponent setDisplay(Material display) {
        return this;
    }

    @Override
    public boolean hasGuiItemStack() {
        return taskComponent.hasGuiItemStack();
    }

    @Override
    public String getDescription() {
        return taskComponent.getDescription();
    }

    /**
     * Description is delegated to the internal TaskComponent
     * */
    @Override
    public TaskComponent setDescription(String description) {
        taskComponent.setDescription(description);
        return this;
    }

    @Override
    public boolean hasReward() {
        return taskComponent.hasReward();
    }

    @Override
    public RewardComponent getReward() {
        return taskComponent.getReward();
    }

    @Override
    public TaskComponent setReward(RewardComponent rewardComponent) {
        taskComponent.setReward(rewardComponent);
        return this;
    }

    /**
     * An invisible task never has a value.
     */
    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public TaskComponent setValue(int value) {
        return this;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();

        return whitelistPlayers.contains(player.getUniqueId())
                && taskComponent.doesAccomplish(lockoutEvent);
    }

    @Override
    public TaskComponent addPlayerCondition(Predicate<HumanEntity> predicate) {
        taskComponent.addPlayerCondition(predicate);
        return this;
    }

    @Override
    public TaskComponent addPlayerCondition(LuaValue predicate) {
        taskComponent.addPlayerCondition(predicate);
        return this;
    }
}
