package stretch.lockout.task;

import org.bukkit.entity.Mob;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface EntityTask {
    TaskComponent addEntityPredicate(Predicate<Mob> condition);
    TaskComponent addEntityPredicate(LuaValue condition);
}
