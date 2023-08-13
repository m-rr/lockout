package stretch.lockout.task;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.lua.LuaBlockPredicate;
import stretch.lockout.task.Task;
import stretch.lockout.util.EventReflectUtil;

import java.util.function.Predicate;

public class TaskMaterial extends Task implements BlockTask {
    final private Material material;
    private Predicate<Block> blockPredicate = (quuz) -> true;

    public TaskMaterial(Class eventClass, Material material, int value, String description) {
        super(eventClass, value, description);
        this.material = material;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (event instanceof BlockEvent blockEvent
                && !blockPredicate.test(blockEvent.getBlock())) {
            return false;
        }

        Material materialFromEvent = EventReflectUtil.getMaterialFromEvent(event);

        return super.doesAccomplish(player, event)
                && materialFromEvent != null
                && materialFromEvent == material;
    }

    @Override
    public TaskComponent addBlockPredicate(Predicate<Block> condition) {
        blockPredicate = blockPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addBlockPredicate(LuaValue condition) {
        addBlockPredicate(new LuaBlockPredicate(condition));
        return this;
    }
}
