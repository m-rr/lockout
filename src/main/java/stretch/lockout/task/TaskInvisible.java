package stretch.lockout.task;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.player.PlayerStat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

public class TaskInvisible implements TaskComponent {
    final private TaskComponent taskComponent;
    //final private Set<HumanEntity> whitelistPlayers = new HashSet<>();
    // includes time of subscription
    final private ConcurrentMap<HumanEntity, Long> whitelistPlayers = new ConcurrentHashMap<>();

    public TaskInvisible(TaskComponent taskComponent) {
        this.taskComponent = taskComponent;
    }

    public TaskInvisible(TaskComponent taskComponent, Collection<HumanEntity> whitelistPlayers) {
        this.taskComponent = taskComponent;
        whitelistPlayers.forEach(this::subscribe);
    }

    public void subscribe(HumanEntity player) {
        whitelistPlayers.put(player, player.getWorld().getGameTime());
    }

    public void unsubscribe(HumanEntity player) {
        whitelistPlayers.remove(player);
    }

    public void unsubscribeAfterTime(long time) {
        whitelistPlayers.entrySet().removeIf(entry -> entry.getKey().getWorld().getGameTime() + 1L >= entry.getValue() + time);
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

    @Override
    public TaskComponent setDescription(String description) {
        description = description;
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

        return whitelistPlayers.containsKey(player)
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
