package stretch.lockout.reward.function;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class RewardVexs implements Consumer<Player> {
    private final int vexCount;
    public RewardVexs(int vexCount) {
        this.vexCount = vexCount;
    }
    @Override
    public void accept(Player player) {
        World world = player.getWorld();
        for (int i = 0; i < vexCount; i++) {
            world.spawnEntity(player.getLocation(), EntityType.VEX);
        }
    }
}
