package stretch.lockout.board;

import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.task.api.TaskComponent;

import java.util.List;


public record BoardDefinition(List<TaskComponent> tasks,
                              List<TaskComponent> tieBreakCounters,
                              List<TaskComponent> mutators,
                              LockoutSettings settings) {
}
