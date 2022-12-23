package stretch.lockout.task.structure.predicate;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.function.Predicate;

public class PanicBinStructurePredicate implements Predicate<Block> {
    @Override
    public boolean test(Block block) {
        var type = block.getType();
        return block.getRelative(BlockFace.DOWN).getType() == Material.COMPOSTER
                && type == Material.OAK_TRAPDOOR
                || type == Material.ACACIA_TRAPDOOR
                || type == Material.BIRCH_TRAPDOOR
                || type == Material.CRIMSON_TRAPDOOR
                || type == Material.DARK_OAK_TRAPDOOR
                || type == Material.IRON_TRAPDOOR
                || type == Material.JUNGLE_TRAPDOOR
                || type == Material.MANGROVE_TRAPDOOR
                || type == Material.SPRUCE_TRAPDOOR
                || type == Material.WARPED_TRAPDOOR;
    }
}
