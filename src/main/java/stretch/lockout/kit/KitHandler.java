package stretch.lockout.kit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.GameStateHandler;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.team.TeamManager;
import stretch.lockout.ui.bar.LockoutTimer;

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
    private final Plugin plugin;
    private final StarterKit starterKit;
    private final CompassKit compassKit;
    private final LockoutSettings settings;
    private final LockoutTimer timer;
    private final TeamManager teamManager;
    private final GameStateHandler stateHandler;
    /** Tracks the System.currentTimeMillis() when a player last respawned, used for kit cooldowns. */
    private final Map<Player, Long> lastRespawn = new HashMap<Player, Long>();

    /**
     * Constructs the KitHandler.
     * Registers this class as an event listener.
     *
     * @author m-rr
     * @param plugin The main Lockout game context, providing access to settings and other managers.
     * @since 2.5.1
     *
     * */
    public KitHandler(@NonNull Plugin plugin, @NonNull TeamManager teamManager, @NonNull GameStateHandler stateHandler, @NonNull LockoutTimer timer, @NonNull LockoutSettings settings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.teamManager = Objects.requireNonNull(teamManager, "teamManager cannot be null");
        this.stateHandler = Objects.requireNonNull(stateHandler, "stateHandler cannot be null");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.timer = Objects.requireNonNull(timer, "timer cannot be null");
        this.starterKit = new StarterKit();
        this.compassKit = new CompassKit(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        teamManager.doToAllPlayers(compassKit::apply);
        teamManager.doToAllPlayers(player -> {
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
        if (stateHandler.getGameState() != GameState.RUNNING) {
            return;
        }

        Player player = playerRespawnEvent.getPlayer();
        if (teamManager.getMappedPlayerStats().containsKey(player)) {
            compassKit.apply(player);

            if (settings.hasRule(LockoutGameRule.RESPAWN_INVULNERABLE)) {
                respawnInvulnerable(player);
            }

            if (settings.hasRule(LockoutGameRule.RESPAWN_KIT)
                    && System.currentTimeMillis() - lastRespawn.get(player)
                        > settings.getRespawnCooldownTime()
                    && timer.hasTimeElapsed(Duration.ofSeconds(settings.getRespawnKitTime() / 20))) {
                starterKit.apply(player);
            }

            lastRespawn.put(player, System.currentTimeMillis());
        }
    }

    private void respawnInvulnerable(Player player) {
        player.setInvulnerable(true);

        player.getScheduler().runDelayed(plugin, scheduledTask -> player.setInvulnerable(false), () -> {}, settings.getRespawnInvulnerabilityTime());
    }
}
