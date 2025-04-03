package stretch.lockout.world;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class AsyncChunkManager {
    protected record ChunkCoordinate(int x, int z) {}
    Map<ChunkCoordinate, ChunkSnapshot> chunks = new HashMap<>();
    World world;

    public AsyncChunkManager(World world) {
        this.world = world;
    }

}
