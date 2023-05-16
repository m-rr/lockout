package stretch.lockout.util;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WorldUtil {

    private static final Set<Material> airTypes = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
    private static final int CHUNK_WIDTH = 15;

    public static Set<Material> materialsInChunk(final ChunkSnapshot chunkSnapshot) {
        return materialsInChunk(chunkSnapshot, -64, 320);
    }

    public static Set<Material> materialsInChunk(final ChunkSnapshot chunkSnapshot, final int lowerBound, final int upperBound) {
        Set<Material> result = new HashSet<>();
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int z = 0; z < CHUNK_WIDTH; z++) {
                for (int y = lowerBound; y < upperBound; y++) {
                    result.add(chunkSnapshot.getBlockType(x, y, z));
                }
            }
        }
        return result;
    }

    public static Set<Material> materialsInChunk(final Chunk chunk) {
        return materialsInChunk(chunk, -64, 320);
    }

    public static Set<Material> materialsInChunk(final Chunk chunk, final int lowerBound, final int upperBound) {
        Set<Material> result = new HashSet<>();
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int z = 0; z < CHUNK_WIDTH; z++) {
                for (int y = lowerBound; y < upperBound; y++) {
                    result.add(chunk.getBlock(x, y, z).getType());
                }
            }
        }
        return result;
    }

    public static boolean findValidChunk(Location location, int edgeLength, Predicate<ChunkSnapshot> chunkPredicate, Player player) {
        World world = location.getWorld();
        int startX = location.getChunk().getX() - Math.floorDiv(edgeLength, 2);
        int startZ = location.getChunk().getZ() - Math.floorDiv(edgeLength, 2);
        for (int x = 0; x < edgeLength; x++) {
            for (int z = 0; z < edgeLength; z++) {
                ChunkSnapshot chunk = world.getChunkAt(startX + x, startZ + z).getChunkSnapshot(false, true, false);
                if (chunkPredicate.test(chunk)) {
                    if (player != null) {
                        player.sendMessage("Chunk at: " + chunk.getX() * 16 + " " + chunk.getZ() * 16);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static Optional<Block> highestBlockAt(final Location location) {
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

    public static record ChunkCoordinate(int x, int z){}
}
