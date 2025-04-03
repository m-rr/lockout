package stretch.lockout.event.state;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import stretch.lockout.event.state.PlayerStateChangeEvent;
import stretch.lockout.game.LockoutContext;

public class PlayerStateChangeTask extends BukkitRunnable {
    private final LockoutContext lockout;
    public PlayerStateChangeTask(final LockoutContext lockout) {
        this.lockout = lockout;
    }

    @Override
    public void run() {
        lockout.getTeamManager().getPlayerStats().stream()
            .map(PlayerStateChangeEvent::new)
            .forEach(playerStateChangeEvent ->
                     Bukkit.getPluginManager().callEvent(playerStateChangeEvent));
    }
}
