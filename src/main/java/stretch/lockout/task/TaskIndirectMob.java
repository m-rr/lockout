package stretch.lockout.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.indirect.LockoutIndirectEvent;
import stretch.lockout.lua.LuaEntityPredicate;
import stretch.lockout.lua.LuaMobPredicate;

import java.util.function.Predicate;

// TODO entity predicate does nothing
public class TaskIndirectMob extends Task implements EntityTask {
    private final double radius;
    private final EntityType entityType;
    private final Class chainedEventClass;
    private Predicate<Mob> entityPredicate = (quuz) -> true;
    public TaskIndirectMob(Class eventClass, Class chainedEventClass, EntityType entityType, double radius, int value, String description) {
        super(eventClass, value, description);
        this.radius = radius;
        this.entityType = entityType;
        this.chainedEventClass = chainedEventClass;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (!(event instanceof LockoutIndirectEvent indirectEvent)
                || !(indirectEvent.getChainedEvent() instanceof EntityEvent entityEvent)) {
            return false;
        }

        EntityType chainedEntityType = entityEvent.getEntityType();

        return super.doesAccomplish(player, event)
                && chainedEntityType == entityType
                && indirectEvent.getChainedEvent().getClass().isAssignableFrom(chainedEventClass)
                && radius >= indirectEvent.getDistance();
    }

    @Override
    public TaskComponent addEntityPredicate(Predicate<Mob> condition) {
        playerStatePredicate = playerStatePredicate.and(playerStatePredicate);
        return this;
    }

    @Override
    public TaskComponent addEntityPredicate(LuaValue condition) {
        addEntityPredicate(new LuaMobPredicate(condition));
        return this;
    }
}
