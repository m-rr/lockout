package stretch.lockout.event.state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import stretch.lockout.game.LockoutContext;

public class PlayerStateChangeHandler implements Listener {
    private final LockoutContext lockout;
    public PlayerStateChangeHandler(final LockoutContext lockout) {
        this.lockout = lockout;
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    @EventHandler
    public void onPlayerStateChange(PlayerStateChangeEvent playerStateChangeEvent) {
        Player player = playerStateChangeEvent.getPlayer();
        if (!player.isOnline()) {
            return;
        }
        // Store movement data here
        //PlayerStat playerStat = playerStateChangeEvent.getPlayerStat();

        //lockout.checkTask(player, playerStateChangeEvent);
    }
}
