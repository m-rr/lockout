package stretch.lockout.task.composite;

import org.bukkit.entity.HumanEntity;
import stretch.lockout.task.api.TaskComponent;

import java.util.List;

public final class TaskSet extends TaskComposite {
    public TaskSet(int value) {
        super(value);
        setDescriptionEntryPrefix(" and ");
    }

    public TaskSet(List<TaskComponent> taskComponents, int value) {
        super(taskComponents, value);
        setDescriptionEntryPrefix(" and ");
    }

    public TaskSet(List<TaskComponent> taskComponents, int value, String description) {
        super(taskComponents, value, description);
        setDescriptionEntryPrefix(" and ");
    }

    @Override
    public boolean playerCompletedTask(HumanEntity player) {
        return playerCompletedTasks.containsKey(player)
                && playerCompletedTasks.get(player).size() == taskComponents.size();
    }

}
