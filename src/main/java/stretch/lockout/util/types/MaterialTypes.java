package stretch.lockout.util.types;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

import java.util.Set;

public class MaterialTypes {
    public static final Set<Material> ORE = ImmutableSet.of(Material.EMERALD_ORE, Material.DIAMOND_ORE, Material.IRON_ORE,
            Material.GOLD_ORE, Material.COAL_ORE, Material.REDSTONE_ORE, Material.COPPER_ORE);
    public static final Set<Material> DEEPSLATE_ORE = ImmutableSet.of(Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_COPPER_ORE);
    public static final Set<Material> DISC = ImmutableSet.of(Material.MUSIC_DISC_11, Material.MUSIC_DISC_5, Material.MUSIC_DISC_13,
            Material.MUSIC_DISC_OTHERSIDE, Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_CHIRP,
            Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI, Material.MUSIC_DISC_PIGSTEP,
            Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD);

    public static final Set<Material> TRAPDOOR = ImmutableSet.of(Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR, Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.MANGROVE_TRAPDOOR,
            Material.CHERRY_TRAPDOOR, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR,
            Material.BAMBOO_TRAPDOOR, Material.IRON_TRAPDOOR);
}
