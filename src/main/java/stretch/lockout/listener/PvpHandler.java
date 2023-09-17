package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.RaceGameContext;

public class PvpHandler implements Listener {
    private final RaceGameContext lockout;

    public PvpHandler(final RaceGameContext taskRaceContext) {
        this.lockout = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (lockout.getGameStateHandler().getGameState() != GameState.RUNNING && lockout.getGameStateHandler().getGameState() != GameState.TIEBREAKER) {
            return;
        }

        Player deadPlayer = playerDeathEvent.getEntity();
        Player killer = deadPlayer.getKiller();
        if (killer == null || deadPlayer == null) {
            return;
        }

        var playerStats = lockout.getTeamManager().getMappedPlayerStats();
        if (playerStats.get(deadPlayer).getTeam() == playerStats.get(killer).getTeam()) {
            return;
        }

        lockout.checkTask(killer, playerDeathEvent);
    }
}
