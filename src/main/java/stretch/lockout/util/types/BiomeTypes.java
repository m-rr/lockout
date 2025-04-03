package stretch.lockout.util.types;

import com.google.common.collect.ImmutableSet;
import org.bukkit.block.Biome;

import java.util.Set;

public class BiomeTypes {
    public static final Set<Biome> JUNGLE = ImmutableSet.of(Biome.JUNGLE, Biome.BAMBOO_JUNGLE, Biome.SPARSE_JUNGLE);
    public static final Set<Biome> SNOW = ImmutableSet.of(Biome.SNOWY_TAIGA, Biome.SNOWY_SLOPES, Biome.SNOWY_BEACH,
            Biome.SNOWY_PLAINS, Biome.ICE_SPIKES, Biome.FROZEN_OCEAN, Biome.FROZEN_PEAKS, Biome.FROZEN_RIVER,
            Biome.GROVE, Biome.JAGGED_PEAKS, Biome.DEEP_FROZEN_OCEAN);
    public static final Set<Biome> SWAMP = ImmutableSet.of(Biome.SWAMP, Biome.MANGROVE_SWAMP);
    public static final Set<Biome> SAVANNA = ImmutableSet.of(Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.WINDSWEPT_SAVANNA);
    public static final Set<Biome> BADLANDS = ImmutableSet.of(Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS);
    public static final Set<Biome> DESERT = ImmutableSet.of(Biome.DESERT);
    public static final Set<Biome> CHERRY = ImmutableSet.of(Biome.CHERRY_GROVE);
    public static final Set<Biome> OAK = ImmutableSet.of(Biome.BAMBOO_JUNGLE, Biome.DARK_FOREST, Biome.FOREST,
            Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.PLAINS,
            Biome.RIVER, Biome.SAVANNA, Biome.SWAMP,
            Biome.WOODED_BADLANDS, Biome.WINDSWEPT_FOREST, Biome.MEADOW);
    public static final Set<Biome> SPRUCE = ImmutableSet.of(Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.OLD_GROWTH_PINE_TAIGA,
            Biome.SNOWY_TAIGA, Biome.SNOWY_PLAINS, Biome.TAIGA,
            Biome.WINDSWEPT_FOREST, Biome.GROVE);
    public static final Set<Biome> BIRCH = ImmutableSet.of(Biome.DARK_FOREST, Biome.FOREST, Biome.BIRCH_FOREST,
            Biome.OLD_GROWTH_BIRCH_FOREST, Biome.MEADOW);
    public static final Set<Biome> OCEAN = ImmutableSet.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.DEEP_LUKEWARM_OCEAN,
            Biome.WARM_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.COLD_OCEAN,
            Biome.LUKEWARM_OCEAN);
    public static final Set<Biome> FROZEN_OCEAN = ImmutableSet.of(Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN);
}
