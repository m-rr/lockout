package stretch.lockout.task.manager;

import stretch.lockout.task.TaskComponent;
import stretch.lockout.util.MessageUtil;

import java.util.Optional;

public class TaskManager {
    private TaskCollection[] taskTiers;

    public TaskManager() {
        // Most boards will have regular tasks plus a tiebreaker
        taskTiers = new TaskCollection[2];
    }

    public void addTask(final int tier, TaskComponent task) {
        try {
            taskTiers[tier].addTask(task);
        } catch (IndexOutOfBoundsException e) {
            MessageUtil.consoleLog("Could not add task: [" + task.getDescription() + "] to task tier: "
                    + tier + " Because the tier does not exist.");
        }
    }

    public void tierExpand(final int distance) {
        TaskCollection[] newTiers = new TaskCollection[taskTiers.length + distance];
        System.arraycopy(taskTiers, 0, newTiers, 0, taskTiers.length);
        taskTiers = newTiers;
    }

    public Optional<TaskCollection> getTier(final int tier) {
        return Optional.ofNullable(taskTiers[tier]);
    }
}
