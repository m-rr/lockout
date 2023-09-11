package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

// Ordered List of tasks must be completed in succession.
public final class TaskTHENComposite extends TaskComposite {
    public TaskTHENComposite(int value) {
        super(value);
    }

    public TaskTHENComposite(List<TaskComponent> taskComponents, int value, String description) {
        super(taskComponents, value, description);
        setDescriptionEntryPrefix(" then ");
    }

    @Override
    public boolean playerCompletedTask(HumanEntity player) {
        return playerCompletedTasks.containsKey(player)
                && playerCompletedTasks.get(player).size() == taskComponents.size();
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (!playerCompletedTasks.containsKey(player)) {
            playerCompletedTasks.put(player, new HashSet<>());
        }

        Optional<TaskComponent> optionalTaskComponent = taskComponents.stream()
                .filter(taskComponent -> !playerCompletedTasks.get(player).contains(taskComponent))
                .findFirst();

        if (optionalTaskComponent.isEmpty()) {
            return true;
        }

        TaskComponent nextTask = optionalTaskComponent.get();
        if (nextTask.getEventClasses().contains(event.getClass()) && nextTask.doesAccomplish(player, event)) {
            setPlayerCompletedTasks(player, nextTask);
        }

        return playerCompletedTask(player);
    }
}
