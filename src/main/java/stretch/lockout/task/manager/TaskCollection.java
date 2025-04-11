package stretch.lockout.task.manager;

import org.bukkit.event.Event;
import stretch.lockout.task.hidden.HiddenTask;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.team.player.PlayerStat;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskCollection {
    private final Map<Class<? extends Event>, Set<TaskComponent>> tasks = new HashMap<>();
    private final Queue<TimeCompletableTask> completedTasks = new LinkedList<>();

    public TaskCollection() {
    }

    public TaskCollection(Collection<TaskComponent> taskComponents) {
        taskComponents.forEach(this::addTask);
    }

    public Map<Class<? extends Event>, Set<TaskComponent>> getMappedTasks() {
        return tasks;
    }

    public Set<Class<? extends Event>> getEventClasses() {
        return getMappedTasks().keySet();
    }

    public Set<TaskComponent> getTasks() {
        return tasks.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public void addAllTasks(Collection<TaskComponent> tasksComponents) {
        for (TaskComponent task : tasksComponents) {
            addTask(task);
        }
    }

    public void addTask(TaskComponent taskComponent) {
        var eventClasses = taskComponent.getEventClasses();
        for (var eventClass : eventClasses) {
            if (!tasks.containsKey(eventClass)) {
                tasks.put(eventClass, new HashSet<>(Set.of(taskComponent)));
            } else {
                Set<TaskComponent> taskSet = tasks.get(eventClass);
                taskSet.add(taskComponent);
            }
        }
    }

    public void removeAllTasks() {
        getTasks().forEach(this::removeTask);
    }

    public void removeTask(TaskComponent taskComponent) {
        var eventClasses = taskComponent.getEventClasses();
        for (var eventClass : eventClasses) {
            if (tasks.containsKey(eventClass)) {
                Set<TaskComponent> taskSet = tasks.get(eventClass);
                taskSet.remove(taskComponent);
                if (taskSet.isEmpty()) {
                    tasks.remove(eventClass);
                }
            }
        }
    }

    public long getTaskCount() {
        return getTasks().stream()
                .filter(taskComponent -> !(taskComponent instanceof HiddenTask))
                .count();
    }

    public Queue<TimeCompletableTask> getCompletedTasks() {
        return completedTasks;
    }

    public void setTaskCompleted(PlayerStat playerStat, TaskComponent task) {
        playerStat.setCompletedTask(task);
        if (task instanceof TimeCompletableTask timeCompletableTask) {
            completedTasks.offer(timeCompletableTask);
        }
    }

    public boolean isTasksLoaded() {
        return !getTasks().isEmpty();
    }

    public int maxPoints() {
        return getTasks().stream()
                .mapToInt(TaskComponent::getValue)
                .sum();
    }

    public int remainingPoints() {
        return getTasks().stream()
                .filter(Predicate.not(TaskComponent::isCompleted))
                .filter(taskComponent -> taskComponent.getValue() > 0)
                .mapToInt(TaskComponent::getValue)
                .sum();
    }

}
