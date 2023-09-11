package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Set;

public class CountDown implements Runnable {

    private final int time;
    private final Iterable<Player> players;
    private final RaceGameContext raceGameContext;
    public CountDown(RaceGameContext raceGameContext, int time, Iterable<Player> players) {
        this.time = time;
        this.players = players;
        this.raceGameContext = raceGameContext;
    }
    @Override
    public void run() {
        if (time > 0) {
            float pitch = time > 3 ? 0.9F : 1.2F;
            players.forEach(player -> {
                player.sendTitle("", ChatColor.GOLD + String.valueOf(time), 5, 1, 5);
                player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1F, pitch);
            });
            Bukkit.getScheduler().scheduleSyncDelayedTask(raceGameContext.getPlugin(), new CountDown(raceGameContext, time - 1, players), 20);
        }
        else {
            players.forEach(player -> {
                player.sendTitle(ChatColor.AQUA + "Start!", "", 1, 5, 10);
                player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
            });
            raceGameContext.setGameState(GameState.RUNNING);
        }
    }
}
