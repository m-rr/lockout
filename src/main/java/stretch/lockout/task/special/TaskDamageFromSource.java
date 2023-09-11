package stretch.lockout.task.special;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.lua.LuaBlockPredicate;
import stretch.lockout.lua.LuaMobPredicate;
import stretch.lockout.task.BlockTask;
import stretch.lockout.task.EntityTask;
import stretch.lockout.task.Task;
import stretch.lockout.task.TaskComponent;

import java.util.function.Predicate;

public class TaskDamageFromSource extends Task implements EntityTask, BlockTask {
    private Predicate<Mob> entityPredicate = (quuz) -> true;
    private Predicate<Block> blockPredicate = (quuz) -> true;
    private final EntityDamageEvent.DamageCause damageCause;
    public TaskDamageFromSource(Class eventClass, EntityDamageEvent.DamageCause damageCause, int value, String description) {
        super(eventClass, value, description);
        this.damageCause = damageCause;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        if (!(event instanceof EntityDamageEvent entityDamageEvent)) {
            return false;
        }

        EntityDamageEvent.DamageCause cause = entityDamageEvent.getCause();
        if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            Entity entity = entityDamageByEntityEvent.getDamager();
            if (hasEntityPredicate() && entity instanceof Mob mob && !entityPredicate.test(mob)) {
                return false;
            }
        }

        if (event instanceof EntityDamageByBlockEvent entityDamageByBlockEvent) {
            Block block = entityDamageByBlockEvent.getDamager();
            if (hasBlockPredicate() && !blockPredicate.test(block)) {
                return false;
            }
        }

        if (hasPlayerPredicate() && !playerStatePredicate.test(player)) {
            return false;
        }

        return cause == damageCause;
    }

    public boolean hasEntityPredicate() {return entityPredicate != null;}
    public boolean hasBlockPredicate() {return blockPredicate != null;}

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

    @Override
    public TaskComponent addEntityPredicate(Predicate<Mob> condition) {
        entityPredicate = entityPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addEntityPredicate(LuaValue condition) {
        addEntityPredicate(new LuaMobPredicate(condition));
        return this;
    }
}
