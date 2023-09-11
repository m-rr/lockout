package stretch.lockout.task.special;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import stretch.lockout.task.Task;

import java.util.function.Predicate;

public class TaskMove extends Task {
    final private Predicate<Location> condition;
    public TaskMove(Class eventClass, Predicate<Location> condition, int value, String description) {
        super(eventClass, value, description);
        this.condition = condition;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (!(event instanceof PlayerMoveEvent moveEvent)) {
            return false;
        }

        return super.doesAccomplish(player, event) && condition.test(moveEvent.getTo());
    }
}
