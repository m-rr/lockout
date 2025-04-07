package stretch.lockout.ui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class PlayerEffect implements Consumer<Player> {
    public static PlayerEffect NEGATIVE_TASK = new PlayerEffect((player) -> {
        player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1F, 1F);
        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1F, 1F);
    });

    public static PlayerEffect POSITIVE_TASK = new PlayerEffect((player)
                -> player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F));


    public static PlayerEffect TESTER = new PlayerEffect((player)
                -> player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 1F, 1F));

    public static PlayerEffect TESTERR = new PlayerEffect((player)
                                                          -> player.playSound(player, Sound.ENTITY_WITHER_DEATH, 0.5F, 0.5F));

    private final Consumer<Player> action;
    public PlayerEffect(Consumer<Player> action) {
        this.action = action;
    }

    @Override
    public void accept(Player player) {
        action.accept(player);
    }
}
