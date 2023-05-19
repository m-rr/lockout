package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.util.HashSet;
import java.util.function.Predicate;

public interface TaskComponent {
    public HashSet<Class> getEventClasses();
    public PlayerStat getScoredPlayer();
    public void setCompletedBy(PlayerStat scoringPlayer);
    public boolean isCompleted();
    public ItemStack getGuiItemStack();
    public TaskComponent setGuiItemStack(ItemStack itemStack);
    public boolean hasGuiItemStack();
    public String getDescription();
    public boolean hasReward();
    public RewardComponent getReward();
    public TaskComponent setReward(RewardComponent rewardComponent);
    public int getValue();
    public boolean doesAccomplish(HumanEntity player, Event event);
    public TaskComponent setPlayerPredicate(Predicate<HumanEntity> predicate);
    public TaskComponent setPlayerPredicate(LuaValue predicate);
}
