package stretch.lockout.event.executor;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Optional;

public interface LockoutWrappedEvent {
    boolean matches(final LockoutWrappedEvent other);
    boolean matches(final Class<? extends Event> eventClass);
    Class<? extends Event> getEventClass();
    Event getEvent();
    Optional<Player> getPlayer();
    Optional<Entity> getEntity();
    Optional<Material> getMaterial();
    Optional<Block> getBlock();
    Optional<PotionEffect> getPotionEffect();
    Optional<EntityDamageEvent.DamageCause> getDamageCause();
    Optional<Block> getBlockDamager();
    Optional<Entity> getEntityDamager();
    Optional<Location> getLocation();
}
