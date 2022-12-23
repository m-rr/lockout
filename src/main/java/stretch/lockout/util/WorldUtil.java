package stretch.lockout.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;
import java.util.Set;

public class WorldUtil {

    private static final Set<Material> airTypes = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    public static Optional<Block> highestBlockAt(Location location) {
        World world = location.getWorld();
        return Optional.ofNullable(highestBlockAt(world, location.getBlockX(),world.getMaxHeight() , location.getBlockZ()));
    }

    private static Block highestBlockAt(World world, int x, int y, int z) {
        Block currentBlock = world.getBlockAt(x, y, z);
        if (!airTypes.contains(currentBlock.getType())) {
            return currentBlock;
        }

        return y > world.getMinHeight() ? highestBlockAt(world, x, y - 1, z) : null;
    }
}
