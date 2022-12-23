package stretch.lockout.reward.function;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class RewardBoomLightning implements Consumer<Player> {

    @Override
    public void accept(Player humanEntity) {
        var world = humanEntity.getWorld();
        world.createExplosion(humanEntity.getLocation(), 1F, true, true);
        world.strikeLightning(humanEntity.getLocation());
    }
}
