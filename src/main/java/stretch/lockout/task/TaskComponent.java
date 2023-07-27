package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.util.HashSet;
import java.util.function.Predicate;

public interface TaskComponent extends PlayerTask {
    HashSet<Class> getEventClasses();
    PlayerStat getScoredPlayer();
    void setCompletedBy(PlayerStat scoringPlayer);
    boolean isCompleted();
    ItemStack getGuiItemStack();
    TaskComponent setGuiItemStack(ItemStack itemStack);
    boolean hasGuiItemStack();
    String getDescription();
    boolean hasReward();
    RewardComponent getReward();
    TaskComponent setReward(RewardComponent rewardComponent);
    int getValue();
    boolean doesAccomplish(HumanEntity player, Event event);
    //public TaskComponent addPlayerPredicate(Predicate<HumanEntity> predicate);
    //public TaskComponent addPlayerPredicate(LuaValue predicate);
}
