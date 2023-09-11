package stretch.lockout.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.luaj.vm2.LuaValue;
import stretch.lockout.lua.LuaMobPredicate;
import stretch.lockout.util.EventReflectUtil;

import java.util.function.Predicate;

public class TaskMob extends Task implements EntityTask {
    private final EntityType entityType;
    private Predicate<Mob> entityPredicate = (quuz) -> true;
    public TaskMob(Class eventClass, EntityType entityType, int value, String description) {
        super(eventClass, value, description);
        this.entityType = entityType;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        Entity entity = (Entity) EventReflectUtil.getEntityFromEvent(event);

        if (entity == null ||
                (hasEntityPredicate() && entity instanceof Mob mob && !entityPredicate.test(mob))) {
            return false;
        }

        return super.doesAccomplish(player, event)
                && entity.getType() == entityType;
    }

    public boolean hasEntityPredicate() {
        return entityPredicate != null;
    }

    @Override
    public TaskComponent addEntityPredicate(Predicate<Mob> condition) {
        entityPredicate = entityPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addEntityPredicate(LuaValue condition) {
        addEntityPredicate(new LuaMobPredicate(condition));
        return this;
    }
}
