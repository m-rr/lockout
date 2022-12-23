package stretch.lockout.reward.function;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class RewardXP implements Consumer<Player> {
    private final int levels;
    public RewardXP(int levels) {
        this.levels = levels;
    }
    @Override
    public void accept(Player player) {
        player.giveExpLevels(levels);
    }
}
