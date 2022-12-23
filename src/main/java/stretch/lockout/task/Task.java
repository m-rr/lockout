package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.util.HashSet;
import java.util.function.Predicate;

public class Task implements TaskComponent {
    protected Class eventClass;
    protected int value;
    protected String description;
    protected RewardComponent reward;
    protected ItemStack guiItemStack;
    protected PlayerStat scoredPlayer;
    protected Predicate<HumanEntity> playerStatePredicate;

    // Class should be of type org.bukkit.event.Event
    public Task(Class eventClass, int value, String description) {
        this.eventClass = eventClass;
        this.value = value;
        this.description = description;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event)  {
        if (hasPlayerPredicate() && !playerStatePredicate.test(player)) {
            return false;
        }

        return event.getClass().isAssignableFrom(eventClass);
    }
    public void setCompletedBy(PlayerStat scoringPlayer) {
        this.scoredPlayer = scoringPlayer;
    }
    public PlayerStat getScoredPlayer() {return this.scoredPlayer;}
    public boolean isCompleted() {
        return scoredPlayer != null;
    }
    public boolean hasReward() {return reward != null;}
    public TaskComponent setReward(RewardComponent rewardComponent) {
        this.reward = rewardComponent;
        return this;
    }
    public RewardComponent getReward() {return this.reward;}
    public boolean hasPlayerPredicate() {return playerStatePredicate != null;}
    public void setPlayerPredicate(Predicate<HumanEntity> predicate) {playerStatePredicate = predicate;}
    public int getValue() {return this.value;}

    public HashSet<Class> getEventClasses() {
        HashSet<Class> result = new HashSet<>();
        result.add(eventClass);
        return result;
    }

    public String getDescription() {return this.description;}

    public ItemStack getGuiItemStack() {return this.guiItemStack;}
    public boolean hasGuiItemStack() {return this.guiItemStack != null;}
    public TaskComponent setGuiItemStack(ItemStack itemStack) {
        this.guiItemStack = itemStack;
        return this;
    }
}