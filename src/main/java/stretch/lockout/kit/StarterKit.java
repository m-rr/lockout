package stretch.lockout.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


/**
 * Provides the player with a {@link Material#STONE_PICKAXE}, {@link Material#STONE_AXE},
 * 32 {@link Material#DIRT} and 10 {@link Material#BREAD}.
 * <p>
 *     This is provided to the player based on {@link stretch.lockout.game.state.LockoutSettings}
 *     in {@link KitHandler}.
 * </p>
 *
 * @author m-rr
 * @version @projectVersion@
 * @see KitHandler
 * @see CompassKit
 * @see Kit
 * @since 2.5.1
 * */
public class StarterKit implements Kit {
    @Override
    public void apply(@NotNull Player player) {
        player.getInventory().addItem(
                new ItemStack(Material.STONE_PICKAXE),
                new ItemStack(Material.STONE_AXE),
                new ItemStack(Material.DIRT, 32),
                new ItemStack(Material.BREAD, 10)
        );
    }
}
