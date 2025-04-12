package stretch.lockout.kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.game.state.GameStateManaged;
import stretch.lockout.util.LockoutLogger;

import java.util.Objects;

/**
 * Provides the essential Lockout compass, the primary
 * tool players use to interact with the game's user interface elements like
 * team selection and task viewing.
 * <p>
 * This kit ensures every player receives a distinct, plugin-specific compass,
 * differentiated from standard compasses via metadata (like its name and a
 * hidden enchantment), preventing accidental loss and ensuring correct plugin
 * interactions.
 * </p>
 *
 * @author m-rr
 * @version @projectVersion@
 * @see Kit
 * @see KitHandler
 * @since 2.5.1
 */
public class CompassKit extends GameStateManaged implements Kit {
    private final Plugin plugin;

    public CompassKit(@NonNull Plugin plugin) {
        super(plugin);
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }
    /**
     * Equips the specified player with the essential Lockout compass, ensuring
     * they have the necessary tool to interact with the game's UI.
     * <p>
     * This method guarantees the player has at least one instance of the
     * specially configured compass in their inventory. It avoids adding
     * duplicates if the player already possesses the required item, preventing
     * inventory clutter. The compass is configured with specific metadata to
     * distinguish it for plugin use.
     * </p>
     * @author m-rr
     * @param player The player to equip with the Lockout compass.
     * @since 2.5.1
     */
    @Override
    public void apply(@NonNull Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = compass.getItemMeta();

        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Lockout");
            compass.setItemMeta(itemMeta);
        }

        Inventory inv = player.getInventory();
        if (!inv.contains(compass)) {
            inv.addItem(compass);
        }

    }
}
