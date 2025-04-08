package stretch.lockout.kit;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Basically a {@link java.util.function.Consumer<Player>} which ideally supplies inventory items
 * to a {@link Player}, but really anything could be applied such as a {@link org.bukkit.potion.PotionEffect}.
 *
 * @author m-rr
 * @version @projectVersion@
 * @see CompassKit
 * @see StarterKit
 * @see KitHandler
 * @since 2.5.1
 * */
public interface Kit {
    /**
     *
     * @author m-rr
     * @since 2.5.1
     * */
    void apply(@NonNull Player player);
}
