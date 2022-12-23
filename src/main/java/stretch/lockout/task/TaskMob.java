package stretch.lockout.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import stretch.lockout.util.EventReflectUtil;

import java.util.function.Predicate;

public class TaskMob extends Task {
    private final EntityType entityType;
    private Predicate<Mob> condition;
    private Predicate<HumanEntity> targetPlayerCondition;
    public TaskMob(Class eventClass, EntityType entityType, int value, String description) {
        super(eventClass, value, description);
        this.entityType = entityType;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        Entity entity = (Entity) EventReflectUtil.getEntityFromEvent(event);

        if (entity == null ||
                (hasEntityPredicate() && entity instanceof Mob mob && !condition.test(mob)) ||
                (hasTargetPlayerPredicate() && entity instanceof HumanEntity humanEntity && !targetPlayerCondition.test(humanEntity))) {
            return false;
        }

        return super.doesAccomplish(player, event)
                && entity.getType() == entityType;
    }

    public boolean hasEntityPredicate() {
        return condition != null;
    }
    public boolean hasTargetPlayerPredicate() {return targetPlayerCondition != null;}

    public void setEntityPredicate(Predicate<Mob> mobPredicate) {
        condition = mobPredicate;
    }

    public void setTargetPlayerPredicate(Predicate<HumanEntity> playerPredicate) {
        targetPlayerCondition = playerPredicate;
    }
}
