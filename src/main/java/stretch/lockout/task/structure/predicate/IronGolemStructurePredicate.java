package stretch.lockout.task.structure.predicate;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.function.Predicate;

public class IronGolemStructurePredicate implements Predicate<Block> {
    @Override
    public boolean test(Block block) {
        Block centerIron = block.getRelative(BlockFace.DOWN);
        return block.getType() == Material.CARVED_PUMPKIN
                && centerIron.getType() == Material.IRON_BLOCK
                && (centerIron.getRelative(BlockFace.NORTH).getType() == Material.IRON_BLOCK
                && centerIron.getRelative(BlockFace.SOUTH).getType() == Material.IRON_BLOCK)
                || (centerIron.getRelative(BlockFace.EAST).getType() == Material.IRON_BLOCK
                && centerIron.getRelative(BlockFace.WEST).getType() == Material.IRON_BLOCK)
                && centerIron.getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK;

    }
}
