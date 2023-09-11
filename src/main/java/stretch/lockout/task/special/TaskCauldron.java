package stretch.lockout.task.special;

import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.task.BlockTask;
import stretch.lockout.task.Task;
import stretch.lockout.task.TaskComponent;

import java.util.function.Predicate;

public class TaskCauldron extends Task {
    private CauldronLevelChangeEvent.ChangeReason changeReason;
    private TaskCauldron(Class eventClass, int value, String description) {
        super(eventClass, value, description);
    }

    public TaskCauldron(CauldronLevelChangeEvent.ChangeReason changeReason, int value, String description) {
        super(CauldronLevelChangeEvent.class, value, description);
        this.changeReason = changeReason;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (!(event instanceof CauldronLevelChangeEvent levelChangeEvent)) {
            return false;
        }
        return levelChangeEvent.getReason() == changeReason
                && super.doesAccomplish(player, event);
    }

}
