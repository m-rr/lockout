package stretch.lockout.ui.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.state.GameState;

public class CountDownSequence implements Runnable {

    private final int time;
    private final Iterable<Player> players;
    private final LockoutContext lockout;
    public CountDownSequence(LockoutContext raceGameContext, int time, Iterable<Player> players) {
        this.time = time;
        this.players = players;
        this.lockout = raceGameContext;
    }
    @Override
    public void run() {
        if (time > 0) {
            float pitch = time > 3 ? 0.9F : 1.2F;
            players.forEach(player -> {
                player.sendTitle("", ChatColor.GOLD + String.valueOf(time), 5, 1, 5);
                player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1F, pitch);
            });
            Bukkit.getScheduler().scheduleSyncDelayedTask(lockout.getPlugin(), new CountDownSequence(lockout, time - 1, players), 20);
        }
        else {
            players.forEach(player -> {
                player.sendTitle(ChatColor.AQUA + "Start!", "", 1, 5, 10);
                player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
            });
            lockout.getGameStateHandler().setGameState(GameState.RUNNING);
        }
    }
}
