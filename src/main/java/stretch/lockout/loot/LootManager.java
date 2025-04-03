package stretch.lockout.loot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.world.NoHighestBlockException;
import stretch.lockout.world.WorldUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class LootManager {
    private World world;
    private final Random random = new Random();
    private final int CHEST_SIZE = 27;
    private double lootSpawnChance = 0D;
    private int spawnRadius;
    public LootManager() {
        throw new UnsupportedOperationException();
    }

    public void setSpawnRadius(int radius) {
        this.spawnRadius = radius;
    }

    public int getSpawnRadius() {
        return spawnRadius;
    }

    public void setWorld(World world) {this.world = world;}

    public double getLootSpawnChance() {
        return lootSpawnChance;
    }

    public void setLootSpawnChance(double chance) {
        lootSpawnChance = chance;
    }

    public void setLootSpawnChance(int chance) {
        lootSpawnChance = (double) chance / 100D;
    }

    // Spawns a loot chest inside the overworld world border
    public void spawnLootBorder() {

        // Spawns at random location offset from corner of border
        WorldBorder worldBorder = world.getWorldBorder();
        int borderLength = (int) worldBorder.getSize();


        Block targetBlock = null;
        try {
            targetBlock = getBlockInArea(worldBorder.getCenter(), borderLength);
        } catch (NoHighestBlockException e) {
            e.printStackTrace();
        }

        // Place chest

        Block chestBlock = targetBlock.getRelative(BlockFace.UP);
        spawnAtBlock(chestBlock);
    }

    public void spawnNearPlayer(HumanEntity player) {
        Block targetBlock = null;
        try {
            targetBlock = getBlockInArea(player.getLocation(), spawnRadius);
        } catch (NoHighestBlockException e) {
            e.printStackTrace();
        }

        Block chestBlock = targetBlock.getRelative(BlockFace.UP);
        spawnAtBlock(chestBlock);
    }

    private Block getBlockInArea(Location location, int side) throws NoHighestBlockException {
        Location corner = location.subtract((side / 2), 0, (side / 2));
        int chestX = random.nextInt(side);
        int chestZ = random.nextInt(side);

        Optional<Block> targetBlock = WorldUtil.highestBlockAt(new Location(world, corner.getBlockX() + chestX, 0, corner.getBlockZ() + chestZ));
        if (targetBlock.isEmpty()) {
            throw new NoHighestBlockException();
        }

        return targetBlock.get().getRelative(BlockFace.UP);
    }

    private void spawnAtBlock(Block chestBlock) {
        chestBlock.setType(Material.CHEST);

        // Set test inventory
        Chest chestData = (Chest) chestBlock.getState();

        if (!fillChest(chestLoot(4), chestData)) {
            Bukkit.getLogger().warning("Chest failed to fill");
        }

        Location chestLoc = chestBlock.getLocation();
        world.strikeLightning(chestLoc);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "LOOT AT " + ChatColor.GOLD + "X: " + ChatColor.GRAY + chestLoc.getX() + ChatColor.GOLD +
                    " Y: " + ChatColor.GRAY + chestLoc.getY() + ChatColor.GOLD + " Z: " + ChatColor.GRAY + chestLoc.getZ());
        });
    }

    private boolean fillChest(ItemStack[] items, Chest chest) {
        Inventory chestInv = chest.getInventory();

        ArrayList<Integer> emptyIndices = new ArrayList<>();
        for (int i = 0; i < chestInv.getSize(); i++) {
            if (chestInv.getItem(i) == null) {
                emptyIndices.add(i);
            }
        }

        if (emptyIndices.size() < items.length) {
            return false;
        }

        for (int i = 0; i < items.length; i++) {
            int j = random.nextInt(emptyIndices.size());
            chestInv.setItem(emptyIndices.get(j), items[i]);
            emptyIndices.remove(j);
        }

        return true;
    }

    private ItemStack[] chestLoot(final int itemCount) {
        ItemStack[] potentialItems = {
                new ItemStack(Material.STONE_AXE),
                new ItemStack(Material.STONE_PICKAXE),
                new ItemStack(Material.IRON_AXE),
                new ItemStack(Material.IRON_PICKAXE),
                new ItemStack(Material.BREAD, 10),
                new ItemStack(Material.GOLDEN_SWORD),
                new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.CHAINMAIL_BOOTS),
                new ItemStack(Material.CHAINMAIL_LEGGINGS),
                new ItemStack(Material.CHAINMAIL_CHESTPLATE),
                new ItemStack(Material.CHAINMAIL_HELMET),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.SHIELD),
                new ItemStack(Material.COAL, 12),
                new ItemStack(Material.FLINT_AND_STEEL),
                new ItemStack(Material.COOKED_BEEF, 5),
                new ItemStack(Material.COBBLESTONE, 32),
                new ItemStack(Material.COBBLESTONE, 32)
        };

        ItemStack[] result = new ItemStack[itemCount];
        for (int i = 0; i < itemCount; i++) {
            result[i] = potentialItems[random.nextInt(potentialItems.length)];
        }

        return result;
    }
}
