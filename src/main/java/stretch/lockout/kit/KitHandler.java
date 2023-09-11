package stretch.lockout.kit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import stretch.lockout.event.PlayerJoinTeamEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;

import java.time.Duration;

public class KitHandler implements Listener {
    private final RaceGameContext lockout;
    private final StarterKit starterKit = new StarterKit();
    private final CompassKit compassKit = new CompassKit();
    public KitHandler(RaceGameContext lockout) {
        this.lockout = lockout;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    @EventHandler
    public void onStarting(StartGameEvent startGameEvent) {
        lockout.getTeamManager().doToAllPlayers(compassKit::apply);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        compassKit.apply(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent playerRespawnEvent) {
        if (lockout.getGameState() != GameState.RUNNING) {
            return;
        }

        Player player = playerRespawnEvent.getPlayer();
        if (lockout.getTeamManager().getMappedPlayerStats().containsKey(player)) {
            lockout.gracePeriod(player);
            compassKit.apply(player);

            if (lockout.getTimer().hasTimeElapsed(Duration.ofMinutes(5))) {
                starterKit.apply(player);
            }
        }
    }
}
