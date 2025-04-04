package stretch.lockout.task.api;

import org.bukkit.entity.HumanEntity;
import org.luaj.vm2.LuaValue;

import java.util.function.Predicate;

public interface PlayerTargetTask {
    TaskComponent addTargetPlayerCondition(Predicate<HumanEntity> condition);

    TaskComponent addTargetPlayerCondition(LuaValue condition);
}
