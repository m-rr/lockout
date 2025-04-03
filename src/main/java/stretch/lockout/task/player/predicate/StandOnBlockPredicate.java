package stretch.lockout.task.player.predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.function.Predicate;

public class StandOnBlockPredicate implements Predicate<Location> {
    final private Material targetBlockMaterial;

    public StandOnBlockPredicate(Material blockMaterial) {
        this.targetBlockMaterial = blockMaterial;
    }

    @Override
    public boolean test(Location location) {
        return location.getBlock().getRelative(BlockFace.DOWN).getType() == targetBlockMaterial;
    }
}
