package stretch.lockout.task.impl.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.lua.LuaBlockPredicate;
import stretch.lockout.task.api.BlockTask;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.base.Task;

import java.util.Optional;
import java.util.function.Predicate;

public class TaskMaterial extends Task implements BlockTask {
    final private Material material;
    private Predicate<Block> blockPredicate = (quuz) -> true;

    public TaskMaterial(Class<? extends Event> eventClass, Material material, int value, String description) {
        super(eventClass, value, description);
        this.material = material;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutWrappedEvent) {

        if (lockoutWrappedEvent.getEventClass() == InventoryClickEvent.class) {
            var ev = (InventoryClickEvent) lockoutWrappedEvent.getEvent();
            //MessageUtil.consoleLog("In taskmat: " + ev.getCurrentItem().getType().name());
            //MessageUtil.sendChat(lockoutWrappedEvent.getPlayer().get(), "In taskmat: " + ev.getCurrentItem().getType().name());
            if (ev.isCancelled()) {
                return false;
            }
        }

        Optional<Material> optionalMaterial = lockoutWrappedEvent.getMaterial();
        if (optionalMaterial.isEmpty()) {
            return false;
        }
        Material materialFromEvent = optionalMaterial.get();

        Optional<Block> optionalBlock = lockoutWrappedEvent.getBlock();
        if (optionalBlock.isPresent() && !blockPredicate.test(optionalBlock.get())) {
            return false;
        }

        return materialFromEvent == material
                && super.doesAccomplish(lockoutWrappedEvent);
    }

    @Override
    public TaskComponent addBlockCondition(Predicate<Block> condition) {
        blockPredicate = blockPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addBlockCondition(LuaValue condition) {
        addBlockCondition(new LuaBlockPredicate(condition));
        return this;
    }
}
