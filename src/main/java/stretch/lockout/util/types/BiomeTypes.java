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
}
