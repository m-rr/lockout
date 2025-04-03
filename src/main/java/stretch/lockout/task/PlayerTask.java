package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface PlayerTask {
    TaskComponent addPlayerCondition(Predicate<HumanEntity> condition);

    TaskComponent addPlayerCondition(LuaValue condition);
}
