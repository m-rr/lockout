package stretch.lockout.kit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Handles applying kits and other items to players during different game phases.
 * This class manages starter kits, compasses, and respawn logic.
 * <p>
 *     This includes giving players the essential compass, providing starter items upon respawn under
 *     certain conditions (rules, time elapsed, cooldown), and granting temporary invulnerability after
 *     respawning if configured.
 * </p>
 * It listens to relevant Bukkit events like player join, respawn, and game start.
 *
 * @author m-rr
 * @version @projectVersion@
 * @see StarterKit
 * @see CompassKit
 * @see LockoutContext#settings()
 * @see LockoutGameRule#RESPAWN_INVULNERABLE
 * @see LockoutGameRule#RESPAWN_KIT
 * @since 2.5.1
 * */
public class KitHandler implements Listener {
    private final LockoutContext lockout;
    private final StarterKit starterKit = new StarterKit();
    private final CompassKit compassKit = new CompassKit();
    /** Tracks the System.currentTimeMillis() when a player last respawned, used for kit cooldowns. */
    private final Map<Player, Long> lastRespawn = new HashMap<Player, Long>();

    /**
     * Constructs the KitHandler.
     * Registers this class as an event listener.
     *
     * @author m-rr
     * @param lockout The main Lockout game context, providing access to settings and other managers.
     * @since 2.5.1
     *
     * */
    public KitHandler(@NonNull LockoutContext lockout) {
        this.lockout = Objects.requireNonNull(lockout, "LockoutContext cannot be null");
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    /**
     * Retrieves the map tracking the last respawn time for players.
     * Key is the Player, Value is the timestamp (System.currentTimeMillis()).
     * Primarily used for testing respawn cooldown logic.
     *
     * @author m-rr
     * @return The map tracking the last respawn times.
     * @since 2.5.1
     * */
    public Map<Player, Long> getLastRespawn() {return lastRespawn;}

    /**
     * Handles the game starting event.
     * Applies the compass kit to all players currently on teams and records their
     * initial "respawn" time for cooldown purposes.
     *
     * @author m-rr
     * @param startGameEvent The event indicating the game is starting.
     * @since 2.5.1
     * */
    @EventHandler
    public void onStarting(StartGameEvent startGameEvent) {
        lockout.getTeamManager().doToAllPlayers(compassKit::apply);
        lockout.getTeamManager().doToAllPlayers(player -> {
                compassKit.apply(player);
                lastRespawn.put(player, System.currentTimeMillis());
            });
    }

    /**
     * Handles the player joining the server.
     * Applies compass kit to the joining player.
     *
     * @author m-rr
     * @param playerJoinEvent The event for a player joining a server.
     * @since 2.5.1
     * */
    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        compassKit.apply(player);
    }

    /**
     * Handles the player respawning during a RUNNING game state.
     * <p>
     *     Applies the following based on game settings:
     *     <ul>
     *         <li>Applies the compass kit.</li>
     *         <li>Grants temporary invulnerability if {@link LockoutGameRule#RESPAWN_INVULNERABLE} is enabled.</li>
     *         <li>Applies the starter kit if {@link LockoutGameRule#RESPAWN_KIT} is enabled,
     *         sufficient game time has passed ({@code respawnKitTime}), and the player's
     *         respawn cooldown ({@code respawnKitCooldown}) has elapsed.</li>
     *     </ul>
     *     Updates the player's last respawn time.
     * </p>
     * Does nothing if the game is not in the RUNNING state or the player is not on a team.
     *
     * @author m-rr
     * @param playerRespawnEvent The event for a player respawning.
     * @since 2.5.1
     * */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent playerRespawnEvent) {
        if (lockout.getGameStateHandler().getGameState() != GameState.RUNNING) {
            return;
        }

        Player player = playerRespawnEvent.getPlayer();
        if (lockout.getTeamManager().getMappedPlayerStats().containsKey(player)) {
            compassKit.apply(player);

            if (lockout.settings().hasRule(LockoutGameRule.RESPAWN_INVULNERABLE)) {
                lockout.gracePeriod(player);
            }

            if (lockout.settings().hasRule(LockoutGameRule.RESPAWN_KIT)
                    && System.currentTimeMillis() - lastRespawn.get(player)
                        > lockout.settings().getRespawnCooldownTime()
                    && lockout.getUiManager().getTimer()
                .hasTimeElapsed(Duration.ofSeconds(lockout.settings().getRespawnKitTime() / 20))) {
                starterKit.apply(player);
            }

            lastRespawn.put(player, System.currentTimeMillis());
        }
    }
}
