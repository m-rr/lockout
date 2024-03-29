package stretch.lockout.lua.table;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.LockoutWrapper;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.reward.RewardType;
import stretch.lockout.util.types.BiomeTypes;
import stretch.lockout.util.types.MaterialTypes;

public class LuaClassBindings implements LuaTableBinding {
    private final RaceGameContext lockout;
    public LuaClassBindings(final RaceGameContext lockout) {
        this.lockout = lockout;
    }
    @Override
    public void injectBindings(LuaTable table) {
        table.set("Bukkit", CoerceJavaToLua.coerce(Bukkit.class));
        table.set("Material", CoerceJavaToLua.coerce(Material.class));
        table.set("Biome", CoerceJavaToLua.coerce(Biome.class));
        table.set("BlockFace", CoerceJavaToLua.coerce(BlockFace.class));
        table.set("Entity", CoerceJavaToLua.coerce(EntityType.class));
        table.set("Enchantment", CoerceJavaToLua.coerce(Enchantment.class));
        table.set("Effect", CoerceJavaToLua.coerce(PotionEffectType.class));
        table.set("DamageCause", CoerceJavaToLua.coerce(EntityDamageEvent.DamageCause.class));
        table.set("CauldronChange", CoerceJavaToLua.coerce(CauldronLevelChangeEvent.ChangeReason.class));
        table.set("DyeColor", CoerceJavaToLua.coerce(DyeColor.class));
        table.set("Color", CoerceJavaToLua.coerce(Color.class));
        table.set("Sound", CoerceJavaToLua.coerce(Sound.class));

        table.set("Lockout", new LockoutWrapper(lockout));
        table.set("GameRule", CoerceJavaToLua.coerce(GameRule.class));
        table.set("RewardType", CoerceJavaToLua.coerce(RewardType.class));
        table.set("GameState", CoerceJavaToLua.coerce(GameState.class));
        table.set("BiomeType", CoerceJavaToLua.coerce(BiomeTypes.class));
        table.set("MaterialType", CoerceJavaToLua.coerce(MaterialTypes.class));
    }
}
