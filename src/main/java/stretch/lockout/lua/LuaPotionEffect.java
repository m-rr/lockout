package stretch.lockout.lua;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

public enum LuaPotionEffect {
    ABSORPTION,
    BAD_OMEN,
    BLINDNESS,
    CONDUIT_POWER,
    DARKNESS,
    DOLPHINS_GRACE,
    FIRE_RESISTANCE,
    GLOWING,
    HASTE,
    HEALTH_BOOST,
    HERO_OF_THE_VILLAGE,
    HUNGER,
    INFESTED,
    INSTANT_DAMAGE,
    INSTANT_HEALTH,
    INVISIBILITY,
    JUMP_BOOST,
    LEVITATION,
    LUCK,
    MINING_FATIGUE,
    NAUSEA,
    NIGHT_VISION,
    OOZING,
    POISON,
    RAID_OMEN,
    REGENERATION,
    RESISTANCE,
    SATURATION,
    SLOW_FALLING,
    SLOWNESS,
    SPEED,
    STRENGTH,
    TRIAL_OMEN,
    UNLUCK,
    WATER_BREATHING,
    WEAKNESS,
    WEAVING,
    WIND_CHARGED,
    WITHER,
    // Old names
    SLOW,
    FAST_DIGGING,
    JUMP,
    INCREASE_DAMAGE;

    public PotionEffectType getEffect() {
        String key = switch(this) {
            case SLOW -> "slowness";
            case FAST_DIGGING -> "haste";
            case JUMP -> "jump_boost";
            case INCREASE_DAMAGE -> "strength";
            case null, default -> this.name().toLowerCase();
        };

        return (PotionEffectType) Registry.EFFECT.get(NamespacedKey.minecraft(key.toLowerCase()));
    }
}
