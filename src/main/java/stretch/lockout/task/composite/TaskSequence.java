package stretch.lockout.task.composite;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.api.TaskComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

// Ordered List of tasks must be completed in succession.
public final class TaskSequence extends TaskComposite {
    public TaskSequence(int value) {
        super(value);
    }

    public TaskSequence(List<TaskComponent> taskComponents, int value, String description) {
        super(taskComponents, value, description);
        setDescriptionEntryPrefix(" then ");
    }

    @Override
    public boolean playerCompletedTask(HumanEntity player) {
        return playerCompletedTasks.containsKey(player)
                && playerCompletedTasks.get(player).size() == taskComponents.size();
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();

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
        if (nextTask.getEventClasses().contains(lockoutEvent.getEventClass()) && nextTask.doesAccomplish(lockoutEvent)) {
            setPlayerCompletedTasks(player, nextTask);
        }

        return playerCompletedTask(player);
    }
}
