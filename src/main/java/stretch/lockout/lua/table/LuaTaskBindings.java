package stretch.lockout.lua.table;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.event.state.PlayerStateChangeEvent;
import stretch.lockout.lua.LuaTaskBuilder;
import stretch.lockout.lua.predicate.LuaBlockPredicate;
import stretch.lockout.task.base.Task;
import stretch.lockout.task.composite.TaskChoice;
import stretch.lockout.task.impl.block.TaskCauldron;
import stretch.lockout.task.impl.block.TaskStructure;
import stretch.lockout.task.impl.entity.TaskDamageFromSource;
import stretch.lockout.task.impl.entity.TaskPvp;
import stretch.lockout.task.impl.player.TaskAdvancement;
import stretch.lockout.task.impl.player.TaskPotion;

import java.util.List;
import java.util.Set;

public class LuaTaskBindings implements LuaTableBinding {

    @Override
    public void injectBindings(LuaTable table) {
        // place(material, value, description, guiMaterial)
        table.set("_place", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, BlockPlaceEvent.class));
            }
        });

        // placeAny(materialType, value, description, guiMaterial)
        table.set("_placeAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskMaterial(args, BlockPlaceEvent.class));
            }
        });

        // destroy(material, value, description, guiMaterial)
        table.set("_destroy", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, BlockBreakEvent.class));
            }
        });

        // destroyAny(materialType, value, description, guiMaterial)
        table.set("_destroyAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskMaterial(args, BlockBreakEvent.class));
            }
        });

        // pickup(material, value, description, guiMaterial)
        table.set("_pickup", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, EntityPickupItemEvent.class));
            }
        });

        // pickupAny(materialType, value, description, guiMaterial)
        table.set("_pickupAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskMaterial(args, EntityPickupItemEvent.class));
            }
        });

        // drop(material, value, description, guiMaterial
        table.set("_drop", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, PlayerDropItemEvent.class));
            }
        });

        // dropAny(material, value, description, guiMaterial)
        table.set("_dropAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskMaterial(args, PlayerDropItemEvent.class));
            }
        });

        // tame(entity, value, description, guiMaterial)
        table.set("_tame", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, EntityTameEvent.class));
            }
        });

        // tameAny(entityGroup, value, description, guiMaterial)
        table.set("_tameAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskEntity(args, EntityTameEvent.class));
            }
        });

        // shear(entity, value, description, guiMaterial)
        table.set("_shear", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, PlayerShearEntityEvent.class));
            }
        });

        // obtain(material, value, description, guiMaterial)
        table.set("_obtain", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));
                return CoerceJavaToLua.coerce(new TaskChoice(List.of(
                        LuaTaskBuilder.createTaskMaterial(args, InventoryClickEvent.class),
                        LuaTaskBuilder.createTaskMaterial(args, FurnaceExtractEvent.class),
                        LuaTaskBuilder.createTaskMaterial(args, CraftItemEvent.class),
                        LuaTaskBuilder.createTaskMaterial(args, PlayerDropItemEvent.class),
                        LuaTaskBuilder.createTaskMaterial(args, EntityPickupItemEvent.class)), value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // obtainAny(materialType, value, description, guiMaterial)
        table.set("_obtainAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                return CoerceJavaToLua.coerce(new TaskChoice(List.of(
                        LuaTaskBuilder.createGroupTaskMaterial(args, InventoryClickEvent.class),
                        LuaTaskBuilder.createGroupTaskMaterial(args, FurnaceExtractEvent.class),
                        LuaTaskBuilder.createGroupTaskMaterial(args, CraftItemEvent.class),
                        LuaTaskBuilder.createGroupTaskMaterial(args, PlayerDropItemEvent.class),
                        LuaTaskBuilder.createGroupTaskMaterial(args, EntityPickupItemEvent.class)), value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // quest(eventName, value, description, guiMaterial)
        table.set("_quest", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String eventName = (String) CoerceLuaToJava.coerce(args.arg(1), String.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                Class eventClass = null;
                try {
                    eventClass = Class.forName(eventName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return CoerceJavaToLua.coerce(new Task(eventClass, value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // playerState(value, description, guiMaterial, pred)
        table.set("_playerState", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int value = (int) CoerceLuaToJava.coerce(args.arg(1), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(2), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(3), Material.class));

                return CoerceJavaToLua.coerce(new Task(PlayerStateChangeEvent.class, value, description)
                        .setGuiItemStack(guiItem)
                        .addPlayerCondition(args.arg(4)));
            }
        });

        // getEffect(potionEffectType, value, description, guiMaterial)
        table.set("_getEffect", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                PotionEffectType potionEffectType = (PotionEffectType) CoerceLuaToJava.coerce(args.arg(1), PotionEffectType.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                return CoerceJavaToLua.coerce(new TaskPotion(potionEffectType, value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // eat(material, value, description, guiMaterial)
        table.set("_eat", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, PlayerItemConsumeEvent.class));
            }
        });

        // eatAny(materialType, value, description, guiMaterial)
        table.set("_eatAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskMaterial(args, PlayerItemConsumeEvent.class));
            }
        });

        // kill(entity, value, description, guiMaterial)
        table.set("_kill", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, EntityDeathEvent.class));
            }
        });

        // killAny(entityGroup, value, description, guiMaterial)
        table.set("_killAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskEntity(args, EntityDeathEvent.class));
            }
        });

        // smelt(material, value, description, guiMaterial)
        table.set("_smelt", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, FurnaceExtractEvent.class));
            }
        });

        // smeltAny(materialType, value, description, guiMaterial)
        table.set("_smeltAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, FurnaceExtractEvent.class));
            }
        });

        // bucket(entity, value, description, guiMaterial)
        table.set("_bucket", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                EntityType entity = (EntityType) CoerceLuaToJava.coerce(args.arg1(), EntityType.class);
                // PlayerBucketEntityEvent does not work correctly on fish.
                return entity == EntityType.AXOLOTL ?
                        CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, PlayerBucketEntityEvent.class)) :
                        CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, PlayerBucketFishEvent.class));
            }
        });

        // hit(entity, value, description, guiMaterial)
        table.set("_hit", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, EntityDamageByEntityEvent.class));
            }
        });

        // hitAny(entityGroup, value, description, guiMaterial)
        table.set("_hitAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskEntity(args, EntityDamageByEntityEvent.class));
            }
        });

        // breed(entity, value, description, guiMaterial)
        table.set("_breed", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, EntityBreedEvent.class));
            }
        });

        // breedAny(entityGroup, value, description, guiMaterial)
        table.set("_breedAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskEntity(args, EntityBreedEvent.class));
            }
        });

        // damageByEntity(entity, damageCause, value, description, guiMaterial)
        table.set("_damageByEntity", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                EntityType entityType = (EntityType) CoerceLuaToJava.coerce(args.arg(1), EntityType.class);
                EntityDamageEvent.DamageCause damageCause = (EntityDamageEvent.DamageCause) CoerceLuaToJava.coerce(args.arg(2), EntityDamageEvent.DamageCause.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(3), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(4), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(5), Material.class));

                return CoerceJavaToLua.coerce(new TaskDamageFromSource(EntityDamageByEntityEvent.class, damageCause, value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // damageByBlock(material, damageCause, value, description, guiMaterial)
        table.set("_damageByBlock", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Material material = (Material) CoerceLuaToJava.coerce(args.arg(1), Material.class);
                EntityDamageEvent.DamageCause damageCause = (EntityDamageEvent.DamageCause) CoerceLuaToJava.coerce(args.arg(2), EntityDamageEvent.DamageCause.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(3), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(4), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(5), Material.class));

                return CoerceJavaToLua.coerce(new TaskDamageFromSource(EntityDamageByBlockEvent.class, damageCause, value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // takeDamage(damageCause, value, description, guiMaterial)
        table.set("_takeDamage", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                EntityDamageEvent.DamageCause damageCause = (EntityDamageEvent.DamageCause) CoerceLuaToJava.coerce(args.arg(1), EntityDamageEvent.DamageCause.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                return CoerceJavaToLua.coerce(new TaskDamageFromSource(EntityDamageEvent.class, damageCause, value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // standOn(material, value, description, guiMaterial)
        table.set("_standOn", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Material material = (Material) CoerceLuaToJava.coerce(args.arg(1), Material.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                return CoerceJavaToLua.coerce(new Task(PlayerStateChangeEvent.class, value, description)
                        .addPlayerCondition(player -> player.getLocation().getBlock()
                                .getRelative(BlockFace.DOWN).getType() == material)
                        .setGuiItemStack(guiItem));

            }
        });

        // standOnAny(materialType, value, description, guiMaterial)
        table.set("_standOnAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Set<Material> materials = (ImmutableSet<Material>) CoerceLuaToJava.coerce(args.arg(1), ImmutableSet.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                return CoerceJavaToLua.coerce(new Task(PlayerMoveEvent.class, value, description)
                        .addPlayerCondition(player -> materials.contains(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType()))
                        .setGuiItemStack(guiItem));
            }
        });

        // interactEntity(entity, value, description, guiMaterial)
        table.set("_interactEntity", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskEntity(args, PlayerInteractEntityEvent.class));
            }
        });

        // interactEntityAny(entityGroup, value, description, guiMaterial)
        table.set("_interactEntityAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskEntity(args, PlayerInteractEntityEvent.class));
            }
        });

        // interactBlock(material, value, description, guiMaterial)
        table.set("_interactBlock", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createTaskMaterial(args, PlayerInteractEvent.class));
            }
        });

        // interactBlockAny(materialType, value, description, guiMaterial)
        table.set("_interactBlockAny", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createGroupTaskMaterial(args, PlayerInteractEvent.class));
            }
        });

        // structure(value, description, guiMaterial, blockPredicate)
        table.set("_structure", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int value = (int) CoerceLuaToJava.coerce(args.arg(1), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(2), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(3), Material.class));
                LuaValue blockPredicate = args.arg(4);

                return CoerceJavaToLua.coerce(new TaskStructure(BlockPlaceEvent.class,
                        new LuaBlockPredicate(blockPredicate), value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // cauldron(reason, value, description, guiMaterial)
        table.set("_cauldron", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                CauldronLevelChangeEvent.ChangeReason reason = (CauldronLevelChangeEvent.ChangeReason)
                        CoerceLuaToJava.coerce(args.arg(1), CauldronLevelChangeEvent.ChangeReason.class);
                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                return CoerceJavaToLua.coerce(new TaskCauldron(reason, value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        // pvp(value, description, guiMaterial)
        table.set("_pvp", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
                int value = (int) CoerceLuaToJava.coerce(luaValue, int.class);
                String description = (String) CoerceLuaToJava.coerce(luaValue1, String.class);
                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(luaValue2, Material.class));

                return CoerceJavaToLua.coerce(new TaskPvp(value, description)
                        .setGuiItemStack(guiItem));
            }
        });

        //advancement(advancement, value, description, guiMaterial)
        table.set("_advancement", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                                //Advancement advancement = (Advancement) CoerceLuaToJava.coerce(args.arg(1), Advancement.class);
                                String title = (String) CoerceLuaToJava.coerce(args.arg(1), String.class);
                                int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                                String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
                                ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

                                return CoerceJavaToLua.coerce(new TaskAdvancement(title, value, description)
                                                              .setGuiItemStack(guiItem));
                        }
                });

    }

}
