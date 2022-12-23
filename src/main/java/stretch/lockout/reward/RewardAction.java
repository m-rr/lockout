package stretch.lockout.reward;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class RewardAction extends RewardLeaf {
    private final Consumer<Player> action;
    public RewardAction(Consumer<Player> consumer, String description) {
        super(description);
        this.action = consumer;
    }

    public RewardAction(Consumer<Player> consumer, RewardType rewardType, String description) {
        super(rewardType, description);
        this.action = consumer;
    }

    @Override
    protected void giveReward(Player player) {
        action.accept(player);
    }
}
