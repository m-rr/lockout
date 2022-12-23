package stretch.lockout.task.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import stretch.lockout.task.Task;

import java.util.function.Predicate;

// There may be a more generic way to do this
public class TaskDamageFromSource extends Task {
    private Predicate<Entity> entityPredicate;
    private Predicate<Block> blockPredicate;
    private EntityDamageEvent.DamageCause damageCause;
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
            if (hasEntityPredicate() && !entityPredicate.test(entity)) {
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

    public void setEntityPredicate(Predicate<Entity> entityPredicate) {
        this.entityPredicate = entityPredicate;
    }
    public void setBlockPredicate(Predicate<Block> blockPredicate) {
        this.blockPredicate = blockPredicate;
    }
    public boolean hasEntityPredicate() {return entityPredicate != null;}
    public boolean hasBlockPredicate() {return blockPredicate != null;}
}
