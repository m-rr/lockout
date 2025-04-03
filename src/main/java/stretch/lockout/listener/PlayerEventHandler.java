package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.platform.Platform;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;

import java.util.Map;
import java.util.UUID;

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
                //player.setScoreboard(lockout.getScoreboardManager().getBoard());
                PlayerStat playerStat = mappedPlayerStats.get(player.getUniqueId());
                playerStat.setPlayer(player);
                lockout.getPlayerTracker().setPlayer(playerStat);
            }
        }

        // If op, check for update
        if (player.isOp() && lockout.settings().hasRule(LockoutGameRule.CHECK_UPDATE)) {
            Bukkit.getScheduler().runTaskAsynchronously(lockout.getPlugin(), () -> {
                String version = lockout.getPlugin().getDescription().getVersion();
                String latest = Platform.latestUpdate();
                if (latest == null || !latest.equals(version)) {
                    MessageUtil.sendChat(player, "Plugin version " +
                            ChatColor.BLUE + latest +
                            ChatColor.DARK_GRAY + " is available; this server is using " +
                            ChatColor.DARK_RED + version);
                    MessageUtil.sendLink(player, Platform.Resource.LOCKOUT_DOWNLOAD_URL,
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
