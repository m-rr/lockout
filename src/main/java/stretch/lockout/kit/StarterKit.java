package stretch.lockout.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StarterKit implements Kit {
    @Override
    public void apply(Player player) {
        player.getInventory().addItem(
                new ItemStack(Material.STONE_PICKAXE),
                new ItemStack(Material.STONE_AXE),
                new ItemStack(Material.DIRT, 32),
                new ItemStack(Material.BREAD, 10)
        );
    }
}
