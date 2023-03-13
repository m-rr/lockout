package stretch.lockout.task;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import stretch.lockout.event.indirect.LockoutIndirectEvent;

public class TaskIndirectMob extends Task {
    private final double radius;
    private final EntityType entityType;
    private final Class chainedEventClass;
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
}
