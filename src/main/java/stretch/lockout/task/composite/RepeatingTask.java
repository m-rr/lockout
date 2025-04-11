package stretch.lockout.task.composite;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.team.player.PlayerStat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class RepeatingTask implements TimeCompletableTask {
    final private TimeCompletableTask taskComponent;
    final private int times;
    final private HashMap<HumanEntity, Integer> completionCount = new HashMap<>();

    public RepeatingTask(TimeCompletableTask taskComponent, int times) {
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
    public Set<Class<? extends Event>> getEventClasses() {
        return taskComponent.getEventClasses();
    }

    @Override
    public PlayerStat getScoredPlayer() {
        return taskComponent.getScoredPlayer();
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
    public Material getDisplay() {
        return taskComponent.getDisplay();
    }

    @Override
    public TaskComponent setDisplay(Material display) {
        return taskComponent.setDisplay(display);
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

    @Override
    public int getValue() {
        return taskComponent.getValue();
    }

    @Override
    public TaskComponent setValue(int value) {
        taskComponent.setValue(value);
        return this;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();

        if (taskComponent.doesAccomplish(lockoutEvent)) {
            setPlayerCompletedTask(player);
        }

        return playerHasCompletedTaskEnoughTimes(player);
    }
}

