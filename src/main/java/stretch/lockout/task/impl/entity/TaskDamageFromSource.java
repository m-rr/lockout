package stretch.lockout.task.impl.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.lua.predicate.LuaBlockPredicate;
import stretch.lockout.lua.predicate.LuaMobPredicate;
import stretch.lockout.task.api.BlockTask;
import stretch.lockout.task.api.EntityTask;
import stretch.lockout.task.base.Task;
import stretch.lockout.task.api.TaskComponent;

import java.util.Optional;
import java.util.function.Predicate;

public class TaskDamageFromSource extends Task implements EntityTask, BlockTask {
    private Predicate<Mob> entityPredicate = (quuz) -> true;
    private Predicate<Block> blockPredicate = (quuz) -> true;
    private final EntityDamageEvent.DamageCause damageCause;

    public TaskDamageFromSource(Class<? extends Event> eventClass, EntityDamageEvent.DamageCause damageCause, int value, String description) {
        super(eventClass, value, description);
        this.damageCause = damageCause;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<EntityDamageEvent.DamageCause> damageCauseOptional = lockoutEvent.getDamageCause();
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (damageCauseOptional.isEmpty() || optionalPlayer.isEmpty()) {
            return false;
        }

        Optional<Entity> optionalEntity = lockoutEvent.getEntityDamager();
        if (optionalEntity.isPresent()) {
            Entity entity = optionalEntity.get();
            if (hasEntityPredicate() && entity instanceof Mob mob && !entityPredicate.test(mob)) {
                return false;
            }
        }

        Optional<Block> optionalBlock = lockoutEvent.getBlockDamager();
        if (optionalBlock.isPresent()) {
            Block block = optionalBlock.get();
            if (hasBlockPredicate() && !blockPredicate.test(block)) {
                return false;
            }
        }

        Player player = optionalPlayer.get();
        if (hasPlayerPredicate() && !playerStatePredicate.test(player)) {
            return false;
        }

        EntityDamageEvent.DamageCause cause = damageCauseOptional.get();
        return cause == damageCause;
    }

    public boolean hasEntityPredicate() {
        return entityPredicate != null;
    }

    public boolean hasBlockPredicate() {
        return blockPredicate != null;
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

    @Override
    public TaskComponent addEntityCondition(Predicate<Mob> condition) {
        entityPredicate = entityPredicate.and(condition);
        return this;
    }

    @Override
    public TaskComponent addEntityCondition(LuaValue condition) {
        addEntityCondition(new LuaMobPredicate(condition));
        return this;
    }
}
