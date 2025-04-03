package stretch.lockout.task.special;

import org.bukkit.event.Event;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.Task;

public class TaskCauldron extends Task {
    private CauldronLevelChangeEvent.ChangeReason changeReason;

    private TaskCauldron(Class<? extends Event> eventClass, int value, String description) {
        super(eventClass, value, description);
    }

    public TaskCauldron(CauldronLevelChangeEvent.ChangeReason changeReason, int value, String description) {
        super(CauldronLevelChangeEvent.class, value, description);
        this.changeReason = changeReason;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Event event = lockoutEvent.getEvent();
        if (!(event instanceof CauldronLevelChangeEvent levelChangeEvent)) {
            return false;
        }

        return levelChangeEvent.getReason() == changeReason
                && super.doesAccomplish(lockoutEvent);
    }

}
