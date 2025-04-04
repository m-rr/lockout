package stretch.lockout.kit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.LockoutContext;

import java.util.Map;
import java.util.HashMap;
import java.time.Duration;

public class KitHandler implements Listener {
    private final LockoutContext lockout;
    private final StarterKit starterKit = new StarterKit();
    private final CompassKit compassKit = new CompassKit();
    private final Map<Player, Long> lastRespawn = new HashMap<Player, Long>();

    public KitHandler(LockoutContext lockout) {
        this.lockout = lockout;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    public Map<Player, Long> getLastRespawn() {return lastRespawn;}

    @EventHandler
    public void onStarting(StartGameEvent startGameEvent) {
        lockout.getTeamManager().doToAllPlayers(compassKit::apply);
        lockout.getTeamManager().doToAllPlayers(player -> {
                compassKit.apply(player);
                lastRespawn.put(player, System.currentTimeMillis());
            });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        compassKit.apply(player);
    }

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
