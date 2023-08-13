package stretch.lockout.task;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import stretch.lockout.task.special.TaskDamageFromSource;
import stretch.lockout.task.special.TaskPotion;

import java.util.Collection;
import java.util.List;

public class TaskCreator {

    public TaskCreator() {

    }

    public TaskComponent doTimes(int times, TaskComponent taskComponent) {
        return new TaskRepeat(taskComponent, times);
    }

    public TaskMaterial pickupItem(Material material , int value, String description) {
        return new TaskMaterial(EntityPickupItemEvent.class, material, value, description);
    }

    public TaskMaterial drop(Material material, int value, String description) {
        return new TaskMaterial(EntityDropItemEvent.class, material, value, description);
    }

    public TaskMaterial place(Material material, int value, String description) {
        return new TaskMaterial(BlockPlaceEvent.class, material, value, description);
    }

    public TaskMaterial breakBlock(Material material, int value, String description) {
        return new TaskMaterial(BlockBreakEvent.class, material, value, description);
    }

    public TaskMob tame(EntityType entityType, int value, String description) {
        return new TaskMob(EntityTameEvent.class, entityType, value, description);
    }

    public TaskMob shear(EntityType entityType, int value, String description) {
        return new TaskMob(PlayerShearEntityEvent.class, entityType, value, description);
    }

    public TaskORComposite obtain(Material material, int value, String description) {
        return new TaskORComposite(List.of(new TaskMaterial(InventoryClickEvent.class, material, 0, ""),
                new TaskMaterial(FurnaceExtractEvent.class, material, 0, ""),
                new TaskMaterial(CraftItemEvent.class, material, 0, ""),
                new TaskMaterial(EntityPickupItemEvent.class, material, 0, "")),
                value, description);
    }

    public TaskORComposite obtainAny(Collection<Material> materialCollection, int value, String description) {
        TaskORComposite result = new TaskORComposite(value, description);
        for (Material material : materialCollection) {
            result.addTaskComponent(obtain(material, 0, ""));
        }
        return result;
    }

    public Task quest(Class eventClass, int value, String description) {
        return new Task(eventClass, value, description);
    }

    public TaskMaterial eat(Material material, int value, String description) {
        return new TaskMaterial(PlayerItemConsumeEvent.class, material, value, description);
    }

    public TaskPotion acquire(PotionEffectType potionEffectType, int value, String description) {
        return new TaskPotion(potionEffectType, value, description);
    }

    public TaskMob kill(EntityType entityType, int value, String description) {
        return new TaskMob(EntityDeathEvent.class, entityType, value, description);
    }

    public TaskMaterial smelt(Material material, int value, String description) {
        return new TaskMaterial(FurnaceExtractEvent.class, material, value, description);
    }

    public TaskMob bucket(EntityType entityType, int value, String description) {
        // Fish do not work with bucketEntityEvent for some reason.
        Class eventClass = PlayerBucketFishEvent.class;
        if (entityType == EntityType.AXOLOTL) {
            eventClass = PlayerBucketEntityEvent.class;
        }

        return new TaskMob(eventClass, entityType, value, description);
    }

    public TaskMob hit(EntityType entityType, int value, String description) {
        return new TaskMob(EntityDamageByEntityEvent.class, entityType, value, description);
    }

    public TaskDamageFromSource damagedByEntity(EntityDamageEvent.DamageCause damageCause, int value, String description) {
        return new TaskDamageFromSource(EntityDamageByEntityEvent.class, damageCause, value, description);
    }

    public TaskDamageFromSource damagedByBlock(EntityDamageEvent.DamageCause damageCause, int value, String description) {
        return new TaskDamageFromSource(EntityDamageByBlockEvent.class, damageCause, value, description);
    }

    public TaskDamageFromSource takeDamage(EntityDamageEvent.DamageCause damageCause, int value, String description) {
        return new TaskDamageFromSource(EntityDamageEvent.class, damageCause, value, description);
    }

    public TaskMob interactEntity(EntityType entityType, int value, String description) {
        return new TaskMob(PlayerInteractEntityEvent.class, entityType, value, description);
    }

    public TaskMaterial interactBlock(Material material, int value, String description) {
        return new TaskMaterial(PlayerInteractEvent.class, material, value, description);
    }
}
