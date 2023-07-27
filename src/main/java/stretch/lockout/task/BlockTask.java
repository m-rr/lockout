package stretch.lockout.task;

import org.bukkit.block.Block;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface BlockTask {
    TaskComponent addBlockPredicate(Predicate<Block> condition);
    TaskComponent addBlockPredicate(LuaValue condition);
}
