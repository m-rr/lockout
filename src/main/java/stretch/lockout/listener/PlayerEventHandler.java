package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.platform.Platform;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.util.LockoutLogger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Ensures proper plugin function when players join and leave in the general case.
 *
 * @author m-rr
 * @version @projectVersion@
 * @since 2.5.1
 * */
public class PlayerEventHandler implements Listener {
    private final LockoutContext lockout;
    public PlayerEventHandler(LockoutContext taskRaceContext) {
        this.lockout = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {

        Player player = joinEvent.getPlayer();
        TeamManager teamManager = lockout.getTeamManager();

        // Reattach pointer to appropriate PlayerStat
        if (teamManager.isPlayerOnTeam(player)) {
            Map<UUID, PlayerStat> mappedPlayerStats = teamManager.getUUIDMappedPlayerStats();

            if (mappedPlayerStats.containsKey(player.getUniqueId())) {
                PlayerStat playerStat = mappedPlayerStats.get(player.getUniqueId());
                playerStat.setPlayer(player);
                lockout.getPlayerTracker().setPlayer(playerStat);
            }
        }

        // If op, check for plugin update
        if (player.isOp() && lockout.settings().hasRule(LockoutGameRule.CHECK_UPDATE)) {
            Bukkit.getAsyncScheduler().runNow(lockout.getPlugin(), task -> {
                String version = lockout.getPlugin().getDescription().getVersion();
                Optional<String> latest = Platform.latestUpdate();
                if (latest.isEmpty() || !latest.get().equals(version)) {
                    LockoutLogger.sendChat(player, "Plugin version " +
                            ChatColor.BLUE + latest.get() +
                            ChatColor.DARK_GRAY + " is available; this server is using " +
                            ChatColor.DARK_RED + version);
                    LockoutLogger.sendLink(player, Platform.Resource.LOCKOUT_DOWNLOAD_URL,
                            ChatColor.UNDERLINE + "Click here to download the latest update.");
                }
            });
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent quitEvent) {
        // Game should end if all players are disconnected
        // This is when the last player disconnects, and they may rejoin, so we wait a minute first.
        // This event is called the same tick as the player disconnecting, so they have not "left" the server
        if (Bukkit.getOnlinePlayers().size() < 2) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(lockout.getPlugin(), () -> {
                if (Bukkit.getOnlinePlayers().size() < 1) {
                    lockout.getGameStateHandler().setGameState(GameState.END);
                }
            }, 1200);
        }
    }

}
