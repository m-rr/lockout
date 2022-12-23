package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import stretch.lockout.game.RaceGameContext;

public record EntityEventHandler(RaceGameContext taskRaceContext) implements Listener {
    public EntityEventHandler(RaceGameContext taskRaceContext) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onCombust(EntityCombustEvent combustEvent) {
        if (combustEvent.isCancelled()) {
            return;
        }

        Entity entity = combustEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, combustEvent);
        }
    }

    @EventHandler
    public void onCombustByBlock(EntityCombustByEntityEvent combustByEntityEvent) {
        if (combustByEntityEvent.isCancelled()) {
            return;
        }

        Entity entity = combustByEntityEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, combustByEntityEvent);
        }
    }

    @EventHandler
    public void onCombustEntity(EntityCombustByEntityEvent combustByEntityEvent) {
        if (combustByEntityEvent.isCancelled()) {
            return;
        }

        var entity = combustByEntityEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, combustByEntityEvent);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent deathEvent) {

        var player = deathEvent.getEntity().getKiller();
        if (player != null) {
            taskRaceContext.checkTask(player, deathEvent);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent damageEvent) {
        if (damageEvent.isCancelled()) {
            return;
        }

        Entity entity = damageEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, damageEvent);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent damageByEntityEvent) {
        if (damageByEntityEvent.isCancelled()) {
            return;
        }

        var entity = damageByEntityEvent.getDamager();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, damageByEntityEvent);
        }
        entity = damageByEntityEvent.getEntity();
        if (entity instanceof  Player player) {
            taskRaceContext.checkTask(player, damageByEntityEvent);
        }
    }

    @EventHandler
    public void onDamageByBlock(EntityDamageByBlockEvent damageByBlockEvent) {
        if (damageByBlockEvent.isCancelled()) {
            return;
        }

        var entity = damageByBlockEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, damageByBlockEvent);
        }
    }

    @EventHandler
    public void onTame(EntityTameEvent tameEvent) {
        if (tameEvent.isCancelled()) {
            return;
        }

        var player = (Player) tameEvent.getOwner();
        taskRaceContext.checkTask(player, tameEvent);
    }

    /* CHANGE LATER */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent projectileHitEvent){
        if (projectileHitEvent.isCancelled()) {
            return;
        }

        if (projectileHitEvent.getEntity() instanceof Player player && projectileHitEvent.getHitBlock() != null) {
            taskRaceContext.checkTask(player, projectileHitEvent);
        }
    }

    @EventHandler
    public void onPiglinBarter(PiglinBarterEvent piglinBarterEvent) {
        if (piglinBarterEvent.isCancelled()) {
            return;
        }

        //var player = piglinBarterEvent.get
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent potionEffectEvent) {
        if (potionEffectEvent.isCancelled()) {
            return;
        }

        var entity = potionEffectEvent.getEntity();
        var potionEffect = potionEffectEvent.getNewEffect();
        if (entity instanceof Player player && potionEffect != null) {
            taskRaceContext.checkTask(player, potionEffectEvent);
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent pickupItemEvent) {
        if (pickupItemEvent.isCancelled()) {
            return;
        }

        var entity = pickupItemEvent.getEntity();
        if (entity instanceof Player player) {
            taskRaceContext.checkTask(player, pickupItemEvent);
        }
    }
}
