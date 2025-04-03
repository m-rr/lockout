package stretch.lockout.lua;

import com.google.common.collect.ImmutableMap;

public class Compatability {

    public static final ImmutableMap<String, String> ENCHANT = ImmutableMap.<String,String>builder()
        .put("arrow_damage", "power")
        .put("arrow_fire", "flame")
        .put("arrow_infinite", "infinity")
        .put("arrow_knockback", "punch")
        .put("damage_all", "sharpness")
        .put("damage_arthropods", "bane_of_arthropods")
        .put("damage_undead", "smite")
        .put("dig_speed", "efficiency")
        .put("durability", "unbreaking")
        .put("loot_bonus_blocks", "fortune")
        .put("loot_bonus_mobs", "looting")
        .put("oxygen", "respiration")
        .put("protection_environmental", "protection")
        .put("protection_explosions", "blast_protection")
        .put("protection_fall", "feather_falling")
        .put("protection_fire", "fire_protection")
        .put("protection_projectile", "projectile_protection")
        .put("water_worker", "aqua_affinity")
        .build();

    public static final ImmutableMap<String, String> POTION = ImmutableMap.<String, String>builder()
        .put("slow", "slowness")
        .put("fast_digging", "haste")
        .put("jump", "jump_boost")
        .put("increase_damage", "strength")
        .build();

}
