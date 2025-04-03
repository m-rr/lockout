package stretch.lockout.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.lua.LuaMobPredicate;

import java.util.Optional;
import java.util.function.Predicate;

public class TaskMob extends Task implements EntityTask {
    private final EntityType entityType;
    private Predicate<Mob> entityPredicate = (quuz) -> true;

    public TaskMob(Class eventClass, EntityType entityType, int value, String description) {
        super(eventClass, value, description);
        this.entityType = entityType;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        //Entity entity = (Entity) EventReflectUtil.getEntityFromEvent(event);
        Optional<Entity> optionalEntity = lockoutEvent.getEntity();
        if (optionalEntity.isEmpty()) {
            return false;
        }
        Entity entity = optionalEntity.get();

        if (hasEntityPredicate() && entity instanceof Mob mob && !entityPredicate.test(mob)) {
            return false;
        }

        return entity.getType() == entityType
                && super.doesAccomplish(lockoutEvent);
        //return super.doesAccomplish(player, event)
        //&& entity.getType() == entityType;
    }

    public boolean hasEntityPredicate() {
        return entityPredicate != null;
    }

    @Override
    public TaskComponent addEntityCondition(Predicate<Mob> condition) {
        entityPredicate = entityPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addEntityCondition(LuaValue condition) {
        addEntityCondition(new LuaMobPredicate(condition));
        return this;
    }
}
