package stretch.lockout.world;

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

    private static void createLine(World world, final Material material, final Location start, final Location delta, final int length) {
        Location curr = start.clone();
        for (int i = 0; i < length; i++) {
            world.getBlockAt(curr).setType(material);
            curr = curr.add(delta);
        }
    }

    public static void createGlassSquare(final Location center, final int size, World world) {
        Location curr = center.subtract((double) size / 2, 0, (double) size / 2);
        final Location delta = new Location(world, 1D, 0D, 0D);
        for (int i = 0; i < size; i++) {
            createLine(world, Material.GLASS, curr, delta, size);
            curr = curr.add(0D, 0D, 1D);
        }
    }
}
