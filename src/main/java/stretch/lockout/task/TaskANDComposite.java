package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;

import java.util.List;

public final class TaskANDComposite extends TaskComposite {
    public TaskANDComposite(int value) {
        super(value);
        setDescriptionEntryPrefix(" and ");
    }

    public TaskANDComposite(List<TaskComponent> taskComponents, int value) {
        super(taskComponents, value);
        setDescriptionEntryPrefix(" and ");
    }

    public TaskANDComposite(List<TaskComponent> taskComponents, int value, String description) {
        super(taskComponents, value, description);
        setDescriptionEntryPrefix(" and ");
    }

    @Override
    public boolean playerCompletedTask(HumanEntity player) {
        return playerCompletedTasks.containsKey(player)
                && playerCompletedTasks.get(player).size() == taskComponents.size();
    }

}
