package stretch.lockout.task;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.lua.LuaHumanEntityPredicate;
import stretch.lockout.team.player.PlayerStat;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public class Task implements TimeCompletableTask {
    protected Class<? extends Event> eventClass;
    protected int value;
    protected String description;
    protected RewardComponent reward;
    protected ItemStack guiItemStack;
    protected PlayerStat scoredPlayer;
    protected Duration timeCompleted;
    protected Location location;
    protected Predicate<HumanEntity> playerStatePredicate = (bar) -> true;

    // Class should be of type org.bukkit.event.Event
    public Task(Class<? extends Event> eventClass, int value, String description) {
        this.eventClass = eventClass;
        this.value = value;
        this.description = description;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent)  {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();

        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();

        if (hasPlayerPredicate() && !playerStatePredicate.test(player)) {
            return false;
        }

        return lockoutEvent.matches(eventClass);
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
    public TaskComponent addPlayerCondition(Predicate<HumanEntity> predicate) {
        playerStatePredicate = playerStatePredicate.and(predicate);
        return this;
    }
    public TaskComponent addPlayerCondition(LuaValue predicate) {
        addPlayerCondition(new LuaHumanEntityPredicate(predicate));
        return this;
    }
    public int getValue() {return this.value;}

    @Override
    public TaskComponent setValue(int value) {
        this.value = value;
        return this;
    }

    public HashSet<Class<? extends Event>> getEventClasses() {
        HashSet<Class<? extends Event>> result = new HashSet<>();
        result.add(eventClass);
        return result;
    }

    public Class<? extends Event> getEventClass() {return eventClass;}

    public String getDescription() {return this.description;}

    @Override
    public TaskComponent setDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemStack getGuiItemStack() {return this.guiItemStack;}
    public boolean hasGuiItemStack() {return this.guiItemStack != null;}
    public TaskComponent setGuiItemStack(ItemStack itemStack) {
        this.guiItemStack = itemStack;
        return this;
    }

    @Override
    public Material getDisplay() {
        return hasGuiItemStack() ? getGuiItemStack().getType() : Material.AIR;
    }

    @Override
    public TaskComponent setDisplay(Material display) {
        return setGuiItemStack(new ItemStack(display));
    }
}
