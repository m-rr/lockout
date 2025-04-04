package stretch.lockout.task.composite;

import org.bukkit.entity.HumanEntity;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.api.TaskComponent;

import java.util.List;

public final class TaskChoice extends TaskComposite {
    public TaskChoice(int value) {
        super(value);
        setDescriptionEntryPrefix(" or ");
    }

    public TaskChoice(int value, String description) {
        super(value);
        this.description = description;
    }

    public TaskChoice(List<TaskComponent> taskComponents, int value) {
        super(taskComponents, value);
        setDescriptionEntryPrefix(" or ");
    }

    public TaskChoice(List<TaskComponent> taskComponents, int value, String description) {
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
