package stretch.lockout.world; // Or your appropriate package

// Import the correct Paper API scheduler interface
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin; // Required for Paper schedulers

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Performs asynchronous searches for blocks of a specific Material within a defined
 * radius of chunks around a central location. Uses Paper API for async chunk loading
 * and searching chunk snapshots off the main thread via the AsyncScheduler.
 */
public class AsyncChunkSearcher {

    private final World world;
    private final int minY;
    private final int maxY;
    private final Plugin plugin; // Reference to your plugin instance

    /**
     * Simple record to hold chunk coordinates.
     */
    private record ChunkCoordinate(int x, int z) {}

    /**
     * Constructs an AsyncChunkSearcher.
     *
     * @param plugin Your plugin instance (required for scheduling tasks).
     * @param world The world to search within.
     * @param minY The minimum Y level (inclusive) to search.
     * @param maxY The maximum Y level (exclusive) to search.
     */
    public AsyncChunkSearcher(Plugin plugin, World world, int minY, int maxY) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.world = Objects.requireNonNull(world, "World cannot be null");

        // Validate Y range against world limits using Paper API
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();
        this.minY = Math.max(worldMinY, minY);
        // Ensure maxY is exclusive and within bounds
        this.maxY = Math.min(worldMaxY, maxY);

        if (this.minY >= this.maxY) {
            throw new IllegalArgumentException(
                    "Corrected minY (" + this.minY + ") must be strictly less than corrected maxY (" + this.maxY + ")"
            );
        }
    }

    /**
     * Asynchronously finds all locations of a specific material within a given chunk radius around a center point.
     * Ensures necessary chunks are loaded before searching. Uses Paper's AsyncScheduler for processing.
     *
     * @param center The center location for the search.
     * @param chunkRadius The radius of chunks around the center chunk to include (0 = only the center chunk).
     * @param material The Material to search for.
     * @param batchSize The number of chunk snapshots to process in each asynchronous search task. Adjust for performance tuning.
     * @return A CompletableFuture containing a List of Locations where the material was found.
     */
    public CompletableFuture<List<Location>> findMaterialNear(
            Location center, int chunkRadius, Material material, int batchSize)
    {
        // --- Input Validation ---
        Objects.requireNonNull(center, "Center location cannot be null");
        if (!center.getWorld().equals(world)) {
            throw new IllegalArgumentException("Center location must be in the same world provided to the searcher.");
        }
        Objects.requireNonNull(material, "Material cannot be null");
        if (chunkRadius < 0) {
            throw new IllegalArgumentException("Chunk radius cannot be negative.");
        }
        final int effectiveBatchSize = Math.max(1, batchSize); // Ensure batch size is at least 1

        // --- 1. Identify Target Chunks ---
        List<ChunkCoordinate> targetCoords = getChunkCoordsInRadius(center, chunkRadius);
        if (targetCoords.isEmpty()) {
            // No chunks to search, return empty list immediately
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // --- 2. Load Necessary Chunks Asynchronously ---
        // Request all target chunks to be loaded (if not already) and get their snapshots.
        List<CompletableFuture<ChunkSnapshot>> loadFutures = targetCoords.stream()
                .map(coord -> world.getChunkAtAsync(coord.x(), coord.z(), true) // true = generate if needed
                        .thenApply(Chunk::getChunkSnapshot) // Get snapshot once chunk is available
                        .exceptionally(ex -> { // Handle potential chunk load failures
                            plugin.getLogger().warning(
                                    "Failed to load chunk snapshot at " + coord.x() + "," + coord.z() + ": " + ex.getMessage()
                            );
                            return null; // Indicate failure, will be filtered later
                        }))
                .toList();

        // --- 3. Wait for Loads & Start Batch Searching ---
        // Use thenComposeAsync to move the coordination logic off the thread that called findMaterialNear
        // and onto a worker thread managed by Paper's async scheduler.
        return CompletableFuture.allOf(loadFutures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(v -> {

                    // Filter out nulls (failed loads) and collect valid snapshots
                    List<ChunkSnapshot> snapshotsToSearch = loadFutures.stream()
                            .map(CompletableFuture::join) // .join() is safe now, allOf() has completed
                            .filter(Objects::nonNull)
                            .toList();

                    if (snapshotsToSearch.isEmpty()) {
                        // All chunk loads failed or none were valid
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }

                    // --- 4. Batch Processing using AsyncScheduler ---
                    List<CompletableFuture<List<Location>>> searchBatchFutures = new ArrayList<>();
                    // Get the correct Paper AsyncScheduler
                    AsyncScheduler asyncScheduler = Bukkit.getServer().getAsyncScheduler();

                    // Split snapshots into batches
                    for (int i = 0; i < snapshotsToSearch.size(); i += effectiveBatchSize) {
                        int end = Math.min(i + effectiveBatchSize, snapshotsToSearch.size());
                        List<ChunkSnapshot> batch = snapshotsToSearch.subList(i, end); // Creates a view, efficient

                        // Create a future for this batch's result
                        CompletableFuture<List<Location>> batchFuture = new CompletableFuture<>();
                        searchBatchFutures.add(batchFuture);

                        // Schedule the search task for this batch on the async scheduler.
                        // AsyncScheduler is suitable here because searching an immutable ChunkSnapshot
                        // doesn't require regional locks or main thread access.
                        // Use runNow to execute the task ASAP on an async thread.
                        asyncScheduler.runNow(plugin, (scheduledTask) -> { // Task provided by runNow
                            try {
                                // Process all snapshots in the batch and collect results
                                List<Location> batchResults = batch.stream()
                                        .flatMap(snapshot -> searchChunkSync(snapshot, material).stream())
                                        .collect(Collectors.toList());
                                batchFuture.complete(batchResults); // Complete the future with results
                            } catch (Throwable t) {
                                plugin.getLogger().severe("Error during async chunk search batch: " + t.getMessage());
                                t.printStackTrace(); // Log detailed error
                                batchFuture.completeExceptionally(t); // Complete exceptionally on error
                            }
                        }); // Associate task with the plugin
                    }

                    // --- 5. Aggregate Results ---
                    // Wait for all batch search futures to complete
                    return CompletableFuture.allOf(searchBatchFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> searchBatchFutures.stream() // Now combine the results
                                    .flatMap(future -> future.join().stream()) // .join() is safe, allOf() completed
                                    .collect(Collectors.toList())); // Collect all locations into a single list

                    // Specify Paper's async scheduler executor for this composition stage
                    // This lambda converts a Runnable into a Consumer<ScheduledTask> for runNow
                }, runnable -> Bukkit.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run()));


    }

    /**
     * Calculates the list of ChunkCoordinates within a square radius around a central location's chunk.
     *
     * @param center The center location.
     * @param radius The chunk radius (0 = only the center chunk).
     * @return A list of ChunkCoordinates to check.
     */
    private List<ChunkCoordinate> getChunkCoordsInRadius(Location center, int radius) {
        if (radius < 0) return Collections.emptyList();

        int centerX = center.getChunk().getX();
        int centerZ = center.getChunk().getZ();
        List<ChunkCoordinate> coords = new ArrayList<>();

        // Calculate coordinates in a square radius
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                coords.add(new ChunkCoordinate(x, z));
            }
        }
        return coords;
    }

    /**
     * Synchronously searches a single ChunkSnapshot for the given material within the defined Y-range.
     * This method is designed to be called from an asynchronous task (like one scheduled on AsyncScheduler).
     *
     * @param snapshot The ChunkSnapshot to search (must be non-null).
     * @param material The Material to look for.
     * @return A list of Locations where the material was found in this chunk. Returns empty list if none found.
     */
    private List<Location> searchChunkSync(ChunkSnapshot snapshot, Material material) {
        List<Location> locations = new ArrayList<>();
        // Calculate base world coordinates for the chunk from its coordinates
        int chunkBaseX = snapshot.getX() << 4; // snapshot.getX() * 16
        int chunkBaseZ = snapshot.getZ() << 4; // snapshot.getZ() * 16

        // Iterate through the blocks within the snapshot's bounds and the specified Y-range
        for (int x = 0; x < 16; x++) { // Local X within chunk (0-15)
            for (int z = 0; z < 16; z++) { // Local Z within chunk (0-15)
                // Use the pre-validated minY and maxY fields of the class
                for (int y = this.minY; y < this.maxY; y++) { // Iterate specified Y range (minY inclusive, maxY exclusive)
                    // Get block data efficiently from the snapshot
                    BlockData blockData = snapshot.getBlockData(x, y, z);
                    if (blockData.getMaterial() == material) {
                        // Found the material, create Location with absolute world coordinates
                        locations.add(new Location(world, chunkBaseX + x, y, chunkBaseZ + z));
                    }
                }
            }
        }
        return locations;
    }

    // No cleanup() method is needed as we are using Paper's managed schedulers.
}