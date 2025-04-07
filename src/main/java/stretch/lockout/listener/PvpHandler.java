package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.state.GameState;

public class PvpHandler implements Listener {
    private final LockoutContext lockout;

    public PvpHandler(final LockoutContext taskRaceContext) {
        this.lockout = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    // TODO pretty sure this will not work correctly
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (lockout.getGameStateHandler().getGameState() != GameState.RUNNING
                && lockout.getGameStateHandler().getGameState() != GameState.TIEBREAKER) {
            return;
        }

        Player deadPlayer = playerDeathEvent.getEntity();
        Player killer = deadPlayer.getKiller();
        if (killer == null || deadPlayer == null) {
            return;
        }

        var playerStats = lockout.getTeamManager().getMappedPlayerStats();
        if (playerStats.get(deadPlayer).getTeam() == playerStats.get(killer).getTeam()) {
        }

        //lockout.checkTask(killer, playerDeathEvent);
    }
}
