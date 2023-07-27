package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface PlayerTask {
    TaskComponent addPlayerPredicate(Predicate<HumanEntity> condition);
    TaskComponent addPlayerPredicate(LuaValue condition);
}
