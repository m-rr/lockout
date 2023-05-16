package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class TaskInvisible implements TaskComponent {
    final private TaskComponent taskComponent;
    final private Set<HumanEntity> whitelistPlayers = new HashSet<>();

    public TaskInvisible(TaskComponent taskComponent) {
        this.taskComponent = taskComponent;
    }

    public TaskInvisible(TaskComponent taskComponent, Collection<HumanEntity> whitelistPlayers) {
        this.taskComponent = taskComponent;
        whitelistPlayers.forEach(this::subscribe);
    }

    public void subscribe(HumanEntity player) {
        whitelistPlayers.add(player);
    }

    public void unsubscribe(HumanEntity player) {
        whitelistPlayers.remove(player);
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
        return whitelistPlayers.contains(player) && taskComponent.doesAccomplish(player, event);
    }

    @Override
    public void setPlayerPredicate(Predicate<HumanEntity> predicate) {
        taskComponent.setPlayerPredicate(predicate);
    }
}
