package stretch.lockout.event.executor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;

import stretch.lockout.util.MessageUtil;

import java.util.Optional;


public class LockoutEventBuilder {
    public static LockoutWrappedEvent build(Event bukkitEvent) {
        return new LockoutEvent(bukkitEvent);
    }

    private record LockoutEvent(Event event) implements LockoutWrappedEvent {

        @Override
        public boolean matches(final LockoutWrappedEvent other) {
            return matches(other.getEventClass());
        }

        @Override
        public boolean matches(Class<? extends Event> eventClass) {
            return getEventClass().isAssignableFrom(eventClass);
        }

        @Override
            public Class<? extends Event> getEventClass() {
                return event.getClass();
            }

            @Override
            public Event getEvent() {
                return event;
            }

            @Override
            public Optional<PotionEffect> getPotionEffect() {
                return switch(event) {
                    case EntityPotionEffectEvent effectEvent
                            when effectEvent.getNewEffect() != null
                            -> Optional.of(effectEvent.getNewEffect());
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<EntityDamageEvent.DamageCause> getDamageCause() {
                return switch(event) {
                    case EntityDamageEvent entityDamageEvent
                        -> Optional.of(entityDamageEvent.getCause());
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<Block> getBlockDamager() {
                return switch(event) {
                    case EntityDamageByBlockEvent entityDamageByBlockEvent
                            when entityDamageByBlockEvent.getDamager() != null
                            -> Optional.of(entityDamageByBlockEvent.getDamager());
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<Entity> getEntityDamager() {
                return switch(event) {
                    case EntityDamageByEntityEvent entityDamageByEntityEvent
                            -> Optional.of(entityDamageByEntityEvent.getDamager());
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<Block> getBlock() {
                return event instanceof BlockEvent blockEvent
                        ? Optional.of(blockEvent.getBlock())
                        : Optional.empty();
            }

            @Override
            public Optional<Player> getPlayer() {
                return switch (event) {
                    case InventoryInteractEvent inventoryInteractEvent -> Optional.of((Player) inventoryInteractEvent.getWhoClicked());
                    case PlayerEvent playerEvent -> Optional.of(playerEvent.getPlayer());
                    case EnchantItemEvent enchantItemEvent -> Optional.of(enchantItemEvent.getEnchanter());
                    case EntityDeathEvent entityDeathEvent
                            when entityDeathEvent.getEntity().getKiller() != null -> Optional.of(entityDeathEvent.getEntity().getKiller());
                    case EntityDamageByEntityEvent entityDamageByEntityEvent
                            when entityDamageByEntityEvent.getDamager() instanceof Player player -> Optional.of(player);
                    case EntityTameEvent entityTameEvent
                            when entityTameEvent.getOwner() instanceof Player player -> Optional.of(player);
                    case EntityBreedEvent entityBreedEvent
                            when entityBreedEvent.getBreeder() instanceof Player player -> Optional.of(player);
                    case EntityEvent entityEvent
                            when entityEvent.getEntity() instanceof Player player -> Optional.of(player);
                    case BlockPlaceEvent blockPlaceEvent -> Optional.of(blockPlaceEvent.getPlayer());
                    case BlockBreakEvent blockBreakEvent -> Optional.of(blockBreakEvent.getPlayer());
                    case BlockIgniteEvent blockIgniteEvent when blockIgniteEvent.getPlayer() != null
                            -> Optional.of(blockIgniteEvent.getPlayer());
                    case CauldronLevelChangeEvent cauldronLevelChangeEvent
                            when cauldronLevelChangeEvent.getEntity() != null
                            && cauldronLevelChangeEvent.getEntity() instanceof Player player
                            -> Optional.of(player);
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<Entity> getEntity() {
                return switch (event) {
                    case EntityCombustByEntityEvent entityCombustByEntityEvent -> Optional.of(entityCombustByEntityEvent.getCombuster());
                    case PlayerInteractEntityEvent playerInteractEntityEvent
                            when playerInteractEntityEvent.getRightClicked() instanceof Entity entity
                            -> Optional.of(entity);
                    case PlayerBucketFishEvent playerBucketFishEvent
                            when playerBucketFishEvent.getEntity() instanceof Entity entity
                            -> Optional.of(entity);
                    case PlayerShearEntityEvent playerShearEntityEvent
                            -> Optional.of(playerShearEntityEvent.getEntity());
                    case EntityEvent entityEvent -> Optional.of(entityEvent.getEntity());
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<Material> getMaterial() {
                return switch (event) {
                    case PlayerInteractEvent playerInteractEvent
                            when playerInteractEvent.getClickedBlock() != null -> Optional.of(playerInteractEvent.getClickedBlock().getType());
                    case PlayerItemBreakEvent playerItemBreakEvent -> Optional.of(playerItemBreakEvent.getBrokenItem().getType());
                    case PlayerItemConsumeEvent playerItemConsumeEvent -> Optional.of(playerItemConsumeEvent.getItem().getType());
                    case PlayerItemDamageEvent playerItemDamageEvent -> Optional.of(playerItemDamageEvent.getItem().getType());
                    case PlayerDropItemEvent playerDropItemEvent -> Optional.of(playerDropItemEvent.getItemDrop().getItemStack().getType());
                    case EnchantItemEvent enchantItemEvent -> Optional.of(enchantItemEvent.getItem().getType());
                    case FoodLevelChangeEvent foodLevelChangeEvent when foodLevelChangeEvent.getItem() != null ->
                            Optional.of(foodLevelChangeEvent.getItem().getType());
                    case EntityPickupItemEvent entityPickupItemEvent -> Optional.of(entityPickupItemEvent.getItem().getItemStack().getType());
                    case InventoryClickEvent inventoryClickEvent when inventoryClickEvent.getCurrentItem() != null -> Optional.of(inventoryClickEvent.getCurrentItem().getType());
                    case CraftItemEvent craftItemEvent when craftItemEvent.getCurrentItem() != null -> Optional.of(craftItemEvent.getCurrentItem().getType());
                    case FurnaceExtractEvent furnaceExtractEvent -> Optional.of(furnaceExtractEvent.getItemType());
                    case BlockPlaceEvent blockPlaceEvent -> Optional.of(blockPlaceEvent.getBlockPlaced().getType());
                    case BlockEvent blockEvent -> Optional.of(blockEvent.getBlock().getType());
                    case null, default -> Optional.empty();
                };
            }

            @Override
            public Optional<Location> getLocation() {
                throw new UnsupportedOperationException();
            }

        }
}
