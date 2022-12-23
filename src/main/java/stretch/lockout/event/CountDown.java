package stretch.lockout.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;

import java.util.Set;

public class CountDown implements Runnable {

    private final int time;
    private final Set<Player> players;
    private final RaceGameContext raceGameContext;
    public CountDown(RaceGameContext raceGameContext, int time, Set<Player> players) {
        this.time = time;
        this.players = players;
        this.raceGameContext = raceGameContext;
    }
    @Override
    public void run() {
        players.forEach(player -> player.sendTitle("", ChatColor.GOLD + String.valueOf(time), 5, 1, 5));
        if (time > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(raceGameContext.getPlugin(), new CountDown(raceGameContext, time - 1, players), 20);
        }
        else {
            players.forEach(player -> {
                player.sendTitle(ChatColor.AQUA + "Start!", "", 1, 5, 10);
                player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_0, 1F, 2.5F);
            });
            raceGameContext.setGameState(GameState.RUNNING);
        }
    }
}
