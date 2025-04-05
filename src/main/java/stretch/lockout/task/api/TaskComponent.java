package stretch.lockout.task.api;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.team.player.PlayerStat;

import java.util.HashSet;

public interface TaskComponent extends PlayerTask {
    HashSet<Class<? extends Event>> getEventClasses();

    PlayerStat getScoredPlayer();

    void setCompletedBy(PlayerStat scoringPlayer);

    boolean isCompleted();

    ItemStack getGuiItemStack();

    TaskComponent setGuiItemStack(ItemStack itemStack);

    Material getDisplay();

    TaskComponent setDisplay(Material display);

    boolean hasGuiItemStack();

    String getDescription();

    TaskComponent setDescription(String description);

    boolean hasReward();

    RewardComponent getReward();

    TaskComponent setReward(RewardComponent rewardComponent);

    int getValue();

    TaskComponent setValue(int value);

    boolean doesAccomplish(final LockoutWrappedEvent event);
}
