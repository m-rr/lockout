package stretch.lockout.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.lua.LuaHumanEntityPredicate;

import java.util.Optional;
import java.util.function.Predicate;

public class TaskPvp extends Task implements PlayerTargetTask {

    private Predicate<HumanEntity> targetPlayerPredicate = (quuz) -> true;

    public TaskPvp(int value, String description) {
        super(org.bukkit.event.entity.PlayerDeathEvent.class, value, description);

    }

    @Override
    public TaskComponent addTargetPlayerCondition(Predicate<HumanEntity> condition) {
        targetPlayerPredicate = targetPlayerPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addTargetPlayerCondition(LuaValue condition) {
        addTargetPlayerCondition(new LuaHumanEntityPredicate(condition));
        return this;
    }

    // TODO this will not work
    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        if (!lockoutEvent.matches(PlayerDeathEvent.class)) {
            return false;
        }

        Optional<Entity> optionalEntity = lockoutEvent.getEntity();
        if (optionalEntity.isEmpty()
                || !(optionalEntity.get() instanceof Player targetPlayer)) {
            return false;
        }

        //return super.doesAccomplish(player, event) && targetPlayerPredicate.test(targetPlayer);
        return targetPlayerPredicate.test(targetPlayer) && super.doesAccomplish(lockoutEvent);
    }
}
