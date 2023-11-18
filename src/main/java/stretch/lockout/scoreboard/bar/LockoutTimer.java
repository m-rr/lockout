package stretch.lockout.scoreboard.bar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class LockoutTimer extends LockoutBar {
    private final BossBar bossBar;
    private Duration time;
    private Duration currTime;
    private BukkitTask timerTask;
    public LockoutTimer() {
        this.bossBar = Bukkit.createBossBar(ChatColor.GOLD + "LOCKOUT", BarColor.BLUE, BarStyle.SOLID);
        this.time = Duration.ofDays(610);
    }

    public void setTime(Duration time) {
        this.time = time;
        this.currTime = Duration.ZERO.plus(time);
    }

    public Duration getTime() {return time;}

    private String readableTime() {
        return String.format("%02d:%02d",
                currTime.toMinutes(), currTime.toSecondsPart());
    }

    public void cancelTimer() {
        if (timerTask != null && Bukkit.getScheduler().isCurrentlyRunning(timerTask.getTaskId())) {
            timerTask.cancel();
        }
    }

    public void startTimer(Plugin plugin, Runnable onComplete) {
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> {
            if (currTime.toSeconds() <= 0) {
                cancelTimer();
                onComplete.run();
            }
            currTime = currTime.minusSeconds(1);
            bossBar.setTitle(ChatColor.GOLD + readableTime());
                }, 1L, 20L);
    }

    public boolean hasTimeElapsed(Duration proposedTime) {
        return time.minus(proposedTime).toSeconds() >= currTime.toSeconds();
    }

    public Duration elapsedTime() {
        return time.minus(currTime);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        cancelTimer();
        setTime(time);
    }

    @Override
    public BossBar bossBar() {
        return bossBar;
    }
}
