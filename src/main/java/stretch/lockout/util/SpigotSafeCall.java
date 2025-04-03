package stretch.lockout.util;


import org.bukkit.Bukkit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Safely calls Spigot API methods that may return null or throw NPEs.
 * Provides fallback values when Spigot APIs behave unexpectedly.
 */
public final class SpigotSafeCall {
    private static final Logger LOGGER = Bukkit.getLogger();
    private static final String WARNING_MSG = "Spigot API returned null/threw NPE. Using fallback.";

    private SpigotSafeCall() {} // Utility class

    /**
     * Calls an unsafe Spigot API method and returns either its result
     * or a fallback value if null/NPE occurs.
     */
    public static <T> T callUnsafeSpigotMethod(Supplier<T> spigotApiCall, T fallback) {
        try {
            T result = spigotApiCall.get();
            return result != null ? result : fallback;
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, WARNING_MSG, e);
            return fallback;
        }
    }

}
