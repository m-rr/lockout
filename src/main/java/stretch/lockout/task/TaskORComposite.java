package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import stretch.lockout.event.executor.LockoutWrappedEvent;

import java.util.List;

public final class TaskORComposite extends TaskComposite {
    public TaskORComposite(int value) {
        super(value);
        setDescriptionEntryPrefix(" or ");
    }

    public TaskORComposite(int value, String description) {
        super(value);
        this.description = description;
    }

    public TaskORComposite(List<TaskComponent> taskComponents, int value) {
        super(taskComponents, value);
        setDescriptionEntryPrefix(" or ");
    }

    public TaskORComposite(List<TaskComponent> taskComponents, int value, String description) {
        super(taskComponents, value);
        this.description = description;
    }

    @Override
    public boolean playerCompletedTask(HumanEntity player) {
        return playerCompletedTasks.containsKey(player);
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        return taskComponents.stream()
                .anyMatch(taskComponent -> taskComponent.doesAccomplish(lockoutEvent));
    }
}
