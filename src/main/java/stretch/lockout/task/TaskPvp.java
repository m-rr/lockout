package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.lua.LuaHumanEntityPredicate;

import java.util.function.Predicate;

public class TaskPvp extends Task implements PlayerTargetTask{

    private Predicate<HumanEntity> targetPlayerPredicate = (quuz) -> true;

    public TaskPvp(int value, String description) {
        super(org.bukkit.event.entity.PlayerDeathEvent.class, value, description);

    }

    @Override
    public TaskComponent addTargetPlayerPredicate(Predicate<HumanEntity> condition) {
        targetPlayerPredicate = targetPlayerPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addTargetPlayerPredicate(LuaValue condition) {
        addTargetPlayerPredicate(new LuaHumanEntityPredicate(condition));
        return this;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {

        if (!(event instanceof PlayerDeathEvent playerDeathEvent)) {
            return false;
        }
        Player targetPlayer = playerDeathEvent.getEntity();
        return super.doesAccomplish(player, event) && targetPlayerPredicate.test(targetPlayer);
    }
}
