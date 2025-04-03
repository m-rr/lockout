package stretch.lockout.task.player.predicate;

import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.function.Predicate;

public class EnterBiomePredicate implements Predicate<Location> {
    final private Biome targetBiome;

    public EnterBiomePredicate(Biome targetBiome) {
        this.targetBiome = targetBiome;
    }

    @Override
    public boolean test(Location location) {
        return location.getBlock().getBiome() == targetBiome;
    }
}
