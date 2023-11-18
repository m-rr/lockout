package stretch.lockout.task;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

public class TaskRepeat implements TimeCompletableTask {
    final private TimeCompletableTask taskComponent;
    final private int times;
    final private HashMap<HumanEntity, Integer> completionCount = new HashMap<>();
    public TaskRepeat(TimeCompletableTask taskComponent, int times) {
        this.taskComponent = taskComponent;
        this.times = times;
    }

    public boolean playerHasCompletedTaskEnoughTimes(HumanEntity player) {
        return completionCount.containsKey(player) && completionCount.get(player) >= times;
    }

    public void setPlayerCompletedTask(HumanEntity player) {
        if (!completionCount.containsKey(player)) {
            completionCount.put(player, 0);
        }

        completionCount.put(player, completionCount.get(player) + 1);
    }

    public int getPlayerScore(HumanEntity player) {
        return completionCount.get(player);
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
    public TaskComponent addPlayerPredicate(Predicate<HumanEntity> predicate) {
        taskComponent.addPlayerPredicate(predicate);
        return this;
    }

    @Override
    public TaskComponent addPlayerPredicate(LuaValue predicate) {
        taskComponent.addPlayerPredicate(predicate);
        return this;
    }

    @Override
    public void setCompletedBy(PlayerStat scoringPlayer) {
        taskComponent.setCompletedBy(scoringPlayer);
    }

    @Override
    public Duration getTimeCompleted() {
        return taskComponent.getTimeCompleted();
    }

    @Override
    public void setLocation(Location loc) {
        taskComponent.setLocation(loc);
    }

    @Override
    public Location getLocation() {
        return taskComponent.getLocation();
    }

    @Override
    public void setTimeCompleted(Duration time) {
        taskComponent.setTimeCompleted(time);
    }

    @Override
    public boolean isCompleted() {
        return taskComponent.isCompleted();
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
        return taskComponent.getValue();
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (taskComponent.doesAccomplish(player, event)) {
            setPlayerCompletedTask(player);
        }

        return playerHasCompletedTaskEnoughTimes(player);
    }
}

