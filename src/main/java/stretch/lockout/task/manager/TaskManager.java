package stretch.lockout.task.manager;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.event.ResetGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.state.GameStateManaged;
import stretch.lockout.game.state.StateResettable;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.task.hidden.HiddenCounterTask;
import stretch.lockout.task.hidden.HiddenTask;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.util.LockoutLogger;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles {@link TaskCollection} of regular tasks, counter tasks, and mutator tasks.
 *
 * @author m-rr
 * @version @projectVersion@
 * @since 2.6.0*/
public class TaskManager extends GameStateManaged {
    private final TaskCollection mainTaskCollection = new TaskCollection();
    private final TaskCollection counterTaskCollection = new TaskCollection();
    private final TaskCollection mutatorTaskCollection = new TaskCollection();
    private final TaskCollection hiddenTaskCollection = new TaskCollection();
    private final TeamManager teamManager;
    //private final Set<Class<? extends Event>> cachedEventClasses = new HashSet<>();
    private Map<Class <? extends Event>, Set<TaskComponent>> cachedTasks = new HashMap<>();
    private final Queue<TimeCompletableTask> completedTasks = new LinkedList<>();
    private int cacheVersion = 0;
    private int taskAdditions = 0;

    public TaskManager(@NonNull Plugin plugin, final TeamManager teamManager) {
        super(plugin);
        this.teamManager = teamManager;
    }

    public boolean containsEventClass(final @NonNull Class <? extends Event> eventClass) {
        ensureValidCache();
        return cachedTasks.containsKey(eventClass);
    }

    public Set<TaskComponent> getTasks(final @NonNull Class <? extends Event> eventClass) {
        ensureValidCache();
        return cachedTasks.get(eventClass);
    }

    public Set<Class <? extends Event>> getEventClasses() {
        ensureValidCache();
        return cachedTasks.keySet();
    }

    private void ensureValidCache() {
        if (!isCacheValid()) {
            cacheVersion = taskAdditions;
            buildTaskEventCache();
        }
    }

    private void buildTaskEventCache() {
        cachedTasks.clear();

        BinaryOperator<Map<Class <? extends Event>, Set<TaskComponent>>> mapAccumulator = (acc, m) -> {
            m.forEach((k, vSet) -> {
                acc.merge(k,
                        new HashSet<>(vSet),
                        (existingSet, newSet) -> {
                            existingSet.addAll(newSet);
                            return existingSet;
                        });
            });
            return acc;
        };

        cachedTasks = Stream.of(mainTaskCollection.getMappedTasks(),
                counterTaskCollection.getMappedTasks(),
                mutatorTaskCollection.getMappedTasks(),
                hiddenTaskCollection.getMappedTasks())
                .reduce(new HashMap<>(), mapAccumulator);
    }

    private boolean isCacheValid() {
        return cacheVersion == taskAdditions;
    }

    public TaskCollection getTasks() {
        return mainTaskCollection;
    }

    public TaskCollection getCounterTasks() {
        return counterTaskCollection;
    }

    public TaskCollection getMutatorTasks() {
        return mutatorTaskCollection;
    }

    public void addTask(@NonNull TaskComponent task) {
        mainTaskCollection.addTask(task);
        taskAdditions++;
    }

    public void addTieBreakCounter(@NonNull TaskComponent task) {
        // These should have no reward
        HiddenCounterTask counterTask = new HiddenCounterTask(task);
        counterTaskCollection.addTask(counterTask);
        taskAdditions++;
    }

    // TODO all players need to be subscribed on game start
    public void addMutator(@NonNull TaskComponent task) {
        // These should always have a reward
        if (!task.hasReward()) {
            LockoutLogger.warning(String.format("Mutator '%s' has no reward, so it is useless!", task.getDescription()));
        }

        HiddenTask mutatorTask = new HiddenTask(task);
        mutatorTaskCollection.addTask(mutatorTask);
        taskAdditions++;
    }

    // TODO call this somewhere
    public void addHiddenTask(@NonNull TaskComponent task) {
        // These should also always have a reward
        if (!task.hasReward()) {
            LockoutLogger.warning(String.format("TaskReward '%s' has no reward, so it is useless!", task.getDescription()));
        }

        HiddenTask hiddenTask = new HiddenTask(task);
        hiddenTaskCollection.addTask(hiddenTask);
        taskAdditions++;
    }

    public void setTaskCompleted(@NonNull PlayerStat playerStat, @NonNull TaskComponent task) {
        playerStat.setCompletedTask(task);
        if (task instanceof TimeCompletableTask timeCompletableTask) {
            completedTasks.offer(timeCompletableTask);
        }
    }

    public int getRemainingPoints() {
        return mainTaskCollection.remainingPoints();
    }

    @Override
    public void onStart(StartGameEvent event) {
        LockoutLogger.debugLog("Searching for mutator tasks");
        teamManager.doToAllPlayers(player -> {
            for (var task : mutatorTaskCollection.getTasks()) {
                if (task instanceof HiddenTask hiddenTask) {
                    LockoutLogger.debugLog(String.format("Subscribing '%s' to hidden task '%s'", player.getName(), hiddenTask.getDescription()));
                    hiddenTask.subscribe(player.getUniqueId());
                }
            }
        });
    }

    @Override
    public void onGameOver(GameOverEvent event) {
        reset();
    }

    @Override
    public void onReset(ResetGameEvent event) {
        reset();
    }

    public void reset() {
        LockoutLogger.debugLog("TaskManager resetting");
        mainTaskCollection.removeAllTasks();
        counterTaskCollection.removeAllTasks();
        mutatorTaskCollection.removeAllTasks();
        hiddenTaskCollection.removeAllTasks();
        cachedTasks.clear();
        cacheVersion = 0;
        taskAdditions = 0;
    }
}
