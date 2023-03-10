package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import stretch.lockout.util.MessageUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

// Ordered List of tasks must be completed in succession.
public final class TaskTHENComposite extends TaskComposite {
    public TaskTHENComposite(int value) {
        super(value);
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
