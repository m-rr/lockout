package stretch.lockout.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import stretch.lockout.event.indirect.LockoutIndirectEvent;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.util.MessageUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class IndirectTaskListener implements Listener {
    private final RaceGameContext taskRaceContext;
    private final double radius;
    public IndirectTaskListener(RaceGameContext taskRaceContext, double radius) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
        this.radius = radius;
    }

    private Optional<Player> closestPlayer(Entity entity) {
        Location entityLocation = entity.getLocation();
        Entity player = entity.getNearbyEntities(radius, radius, radius).stream()
                .filter(targetEntity -> targetEntity.getType() == EntityType.PLAYER)
                .min(new Comparator<Entity>() {
                    @Override
                    public int compare(Entity o1, Entity o2) {
                        double o1Distance = o1.getLocation().distance(entityLocation);
                        double o2Distance = o2.getLocation().distance(entityLocation);
                        if (o1Distance == o2Distance) {
                            return 0;
                        }
                        return o1Distance > o2Distance ? 1 : -1;
                    }
                }).orElse(null);
        return Optional.ofNullable((Player) player);
    }

    @EventHandler
    public void onIndirectEvent(LockoutIndirectEvent indirectEvent) {
        taskRaceContext.checkTask(indirectEvent.getPlayer(), indirectEvent);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
        Entity entity = entityDeathEvent.getEntity();
        Optional<Player> playerOptional = closestPlayer(entity);
        //playerOptional.ifPresent(player -> MessageUtil.sendChat(player, "Closest player."));
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            double distance = entity.getLocation().distance(player.getLocation());
            Bukkit.getPluginManager().callEvent(new LockoutIndirectEvent(player, entityDeathEvent, distance));
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustByBlockEvent combustEvent) {
        Entity entity = combustEvent.getEntity();
        Optional<Player> playerOptional = closestPlayer(entity);

        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            double distance = entity.getLocation().distance(player.getLocation());
            Bukkit.getPluginManager().callEvent(new LockoutIndirectEvent(player, combustEvent, distance));
        }
    }
}
