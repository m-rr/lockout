package stretch.lockout.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.util.*;
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
    public HashSet<Class> getEventClasses() {
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
    public boolean hasGuiItemStack() {
        return taskComponent.hasGuiItemStack();
    }

    @Override
    public String getDescription() {
        return taskComponent.getDescription();
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

    @Override
    public int getValue() {
        // Always has no value.
        return 0;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        return whitelistPlayers.containsKey(player) && taskComponent.doesAccomplish(player, event);
    }

    @Override
    public TaskComponent addPlayerPredicate(Predicate<HumanEntity> predicate) {
        taskComponent.addPlayerPredicate(predicate);
        return this;
    }

    @Override
    public TaskComponent addPlayerPredicate(LuaValue predicate) {
        taskComponent.addPlayerPredicate(predicate);
        return this;
    }
}
