package stretch.lockout.task;

import org.bukkit.entity.Mob;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface EntityTask {
    TaskComponent addEntityCondition(Predicate<Mob> condition);

    TaskComponent addEntityCondition(LuaValue condition);
}
