package stretch.lockout.task.impl.block;

import org.bukkit.block.Block;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.base.Task;

import java.util.Optional;
import java.util.function.Predicate;

public class TaskStructure extends Task {
    final private Predicate<Block> condition;

    public TaskStructure(Class eventClass, Predicate<Block> condition, int value, String description) {
        super(eventClass, value, description);
        this.condition = condition;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<Block> optionalBlock = lockoutEvent.getBlock();
        if (optionalBlock.isEmpty()) {
            return false;
        }
        Block block = optionalBlock.get();

        return lockoutEvent.matches(eventClass) && condition.test(block);
        //return event.getClass() == eventClass && condition.test(blockEvent.getBlock());
    }


}
