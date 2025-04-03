package stretch.lockout.task;

import org.bukkit.block.Block;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface BlockTask {
    TaskComponent addBlockCondition(Predicate<Block> condition);
    TaskComponent addBlockCondition(LuaValue condition);
}
