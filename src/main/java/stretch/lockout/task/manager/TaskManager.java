package stretch.lockout.task.manager;

import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.game.state.StateResettable;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.hidden.HiddenCounterTask;
import stretch.lockout.task.hidden.HiddenTask;

import java.util.Optional;

// TODO make this have TaskCollections -> main, mutator, tiebreak
public class TaskManager implements StateResettable {
    private final TaskCollection mainTaskCollection = new TaskCollection();
    private final TaskCollection counterTaskCollection = new TaskCollection();
    private final TaskCollection mutatorTaskCollection = new TaskCollection();

    public TaskManager() {
        // Most boards will have regular tasks plus a tiebreaker
        //taskTiers = new TaskCollection[2];
    }

    public void addTask(final int tier, TaskComponent task) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*try {
            taskTiers[tier].addTask(task);
        } catch (IndexOutOfBoundsException e) {
            LockoutLogger.consoleLog("Could not add task: [" + task.getDescription() + "] to task tier: "
                    + tier + " Because the tier does not exist.");
        }*/

    }

    public void addTask(@NonNull TaskComponent task) {
        mainTaskCollection.addTask(task);
    }

    public void addTieBreakCounter(@NonNull TaskComponent task) {
        // These should have no reward
        HiddenCounterTask counterTask = new HiddenCounterTask(task);
        counterTaskCollection.addTask(counterTask);
    }

    public void addMutator(@NonNull TaskComponent task) {
        // These should always be completable
        HiddenTask mutatorTask = new HiddenTask(task);
        mutatorTaskCollection.addTask(mutatorTask);
    }

    public void tierExpand(final int distance) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TaskCollection[] newTiers = new TaskCollection[taskTiers.length + distance];
        System.arraycopy(taskTiers, 0, newTiers, 0, taskTiers.length);
        taskTiers = newTiers;*/
    }

    public Optional<TaskCollection> getTier(final int tier) {
        throw new UnsupportedOperationException("Not supported yet.");
        //return Optional.ofNullable(taskTiers[tier]);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
