package stretch.lockout.ui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.player.PlayerStat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TeamSelectionView extends LockoutView implements InventoryHolder {
    private Inventory inventory;
    private final List<LockoutTeam> teams;
    private int size = 9;
    private final String inventoryName = "Select Team";

    private final ItemStack guiItem = new ItemStack(Material.NETHER_STAR);

    private ItemStack defaultItemFactory() {
        return new ItemStack(guiItem);
    }

    public TeamSelectionView() {
        this.inventory = Bukkit.createInventory(this, size, inventoryName);
        this.teams = new ArrayList<>();
    }

    public void addTeam(LockoutTeam team) {
        teams.add(team);
        if (teams.size() > size) {
            ItemStack[] contents = inventory.getContents();
            size += 9;
            inventory = Bukkit.createInventory(this, size, inventoryName);
            inventory.setContents(contents);
        }
    }

    public void update() {
        inventory.clear();
        teams.forEach(team -> {
            ItemStack guiItem = Optional.ofNullable(team.getGuiItem())
                    .orElse(defaultItemFactory());

            ItemMeta itemMeta = guiItem.getItemMeta();
            itemMeta.setDisplayName(team.getName());

            List<String> lore = new LinkedList<>();
            lore.add("Players: " + team.playerCount() + "/" + team.getMaxSize());
            team.getPlayerStats().stream()
                    .map(PlayerStat::getPlayer)
                    .forEach(player -> lore.add(player.getDisplayName()));

            itemMeta.setLore(lore);
            itemMeta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES);

            guiItem.setItemMeta(itemMeta);

            guiItem = team.playerCount() > 0 ?
                    makeSpecialItem(guiItem) :
                    makeRegularItem(guiItem);

            inventory.addItem(guiItem);
        });
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
