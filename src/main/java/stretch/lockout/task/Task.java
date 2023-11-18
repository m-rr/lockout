package stretch.lockout.task;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.lua.LuaHumanEntityPredicate;
import stretch.lockout.team.PlayerStat;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashSet;
import java.util.function.Predicate;

public class Task implements TimeCompletableTask {
    protected Class eventClass;
    protected int value;
    protected String description;
    protected RewardComponent reward;
    protected ItemStack guiItemStack;
    protected PlayerStat scoredPlayer;
    protected Duration timeCompleted;
    protected Location location;
    protected Predicate<HumanEntity> playerStatePredicate = (bar) -> true;

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
    public void setTimeCompleted(Duration time) {
        this.timeCompleted = time;
    }
    public Duration getTimeCompleted() {return this.timeCompleted;}

    @Override
    public void setLocation(Location loc) {
        this.location = loc;
    }

    @Override
    @Nullable
    public Location getLocation() {
        return location;
    }

    public boolean hasReward() {return reward != null;}
    public TaskComponent setReward(RewardComponent rewardComponent) {
        this.reward = rewardComponent;
        return this;
    }
    public RewardComponent getReward() {return this.reward;}
    public boolean hasPlayerPredicate() {return playerStatePredicate != null;}
    public TaskComponent addPlayerPredicate(Predicate<HumanEntity> predicate) {
        playerStatePredicate = playerStatePredicate.and(predicate);
        return this;
    }
    public TaskComponent addPlayerPredicate(LuaValue predicate) {
        addPlayerPredicate(new LuaHumanEntityPredicate(predicate));
        return this;
    }
    public int getValue() {return this.value;}

    public HashSet<Class> getEventClasses() {
        HashSet<Class> result = new HashSet<>();
        result.add(eventClass);
        return result;
    }

    public Class getEventClass() {return eventClass;}

    public String getDescription() {return this.description;}

    public ItemStack getGuiItemStack() {return this.guiItemStack;}
    public boolean hasGuiItemStack() {return this.guiItemStack != null;}
    public TaskComponent setGuiItemStack(ItemStack itemStack) {
        this.guiItemStack = itemStack;
        return this;
    }
}
