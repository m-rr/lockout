package stretch.lockout.task.structure;

import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import stretch.lockout.task.Task;

import java.util.function.Predicate;

public class TaskStructure extends Task {
    final private Predicate<Block> condition;
    public TaskStructure(Class eventClass, Predicate<Block> condition, int value, String description) {
        super(eventClass, value, description);
        this.condition = condition;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (!(event instanceof BlockEvent blockEvent)) {
            return false;
        }

        return event.getClass() == eventClass && condition.test(blockEvent.getBlock());
    }


}
