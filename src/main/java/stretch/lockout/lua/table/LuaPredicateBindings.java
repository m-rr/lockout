package stretch.lockout.lua.table;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import stretch.lockout.lua.LuaPotionEffect;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class LuaPredicateBindings implements LuaTableBinding {
    @Override
    public void injectBindings(LuaTable table) {
        // inMainHand(entity, material)
        table.set("inMainHand", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                return CoerceJavaToLua.coerce(entity.getEquipment().getItemInMainHand().getType() == material);
            }
        });

        // inOffHand(entity, material)
        table.set("inOffHand", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                return CoerceJavaToLua.coerce(entity.getEquipment().getItemInOffHand().getType() == material);
            }
        });

        // inBiome(entity, biome)
        table.set("inBiome", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Biome biome = (Biome) CoerceLuaToJava.coerce(luaValue1, Biome.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getBlock().getBiome() == biome);
            }
        });

        // inBiomeType(entity, biomeType)
        table.set("inBiomeType", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Set<Biome> biomes = (ImmutableSet<Biome>) CoerceLuaToJava.coerce(luaValue1, ImmutableSet.class);
                return CoerceJavaToLua.coerce(biomes.contains(entity.getLocation().getBlock().getBiome()));
            }
        });

        // onBlock(entity, blockMaterial)
        table.set("onBlock", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == material);
            }
        });

        // hasMaxHealth(entity)
        table.set("hasMaxHealth", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                //return CoerceJavaToLua.coerce(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() == entity.getHealth());
                return CoerceJavaToLua.coerce(entity.getAttribute(Attribute.MAX_HEALTH).getValue() == entity.getHealth());
            }
        });

        // wearingArmor(entity, armorMaterial)
        table.set("wearingArmor", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                Set<Material> armor = Arrays.stream(entity.getEquipment().getArmorContents())
                        .map(ItemStack::getType)
                        .collect(Collectors.toSet());
                return CoerceJavaToLua.coerce(armor.contains(material));
            }
        });

        // aboveY(entity, yValue)
        table.set("aboveY", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                double y = (double) CoerceLuaToJava.coerce(luaValue1, double.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getY() >= y);
            }
        });

        // hasPotionEffect(entity, potionEffectType)
        table.set("hasPotionEffect", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                LuaPotionEffect effect = (LuaPotionEffect) CoerceLuaToJava.coerce(luaValue1, LuaPotionEffect.class);
                PotionEffectType potionEffectType = effect.getEffect();
                return CoerceJavaToLua.coerce(entity.getPotionEffect(potionEffectType) != null);
            }
        });

        // onFire(entity)
        table.set("onFire", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.getFireTicks() > 0);
            }
        });

        // inWater(entity)
        table.set("inWater", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.isInWater());
            }
        });

        // isDay(entity)
        table.set("isDay", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                final long NIGHT_START = 13000;
                return CoerceJavaToLua.coerce(entity.getWorld().getTime() < NIGHT_START);
            }
        });

        // onSurface(entity)
        table.set("onSurface", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getBlock().getLightFromSky() == 15);
            }
        });

        // isEntity(entity, targetEntityType)
        table.set("isEntity", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Entity entity = (Entity) CoerceLuaToJava.coerce(luaValue, Entity.class);
                EntityType targetEntityType = (EntityType) CoerceLuaToJava.coerce(luaValue1, EntityType.class);
                return CoerceJavaToLua.coerce(entity.getType() == targetEntityType);
            }
        });
    }
}
