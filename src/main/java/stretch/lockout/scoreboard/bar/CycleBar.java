package stretch.lockout.scoreboard.bar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import stretch.lockout.game.RaceGameContext;

import java.util.List;

public class CycleBar extends LockoutBar {
    private final RaceGameContext lockout;
    private final List<String> barTitles;
    private final BossBar cycleBar;
    private final int cycleTime;
    private int cycleTask = -1;
    public CycleBar(final RaceGameContext lockout, final int cycleTime, List<String> bars) {
        this.lockout = lockout;
        this.barTitles = bars;
        this.cycleTime = cycleTime;
        cycleBar = Bukkit.createBossBar(barTitles.get(0), BarColor.WHITE, BarStyle.SOLID);
    }

    private Runnable cycleRunnable() {
        return new Runnable() {
            int i = 0;
            @Override
            public void run() {
                String title = barTitles.get(i % barTitles.size());
                cycleBar.setTitle(title);
                i++;
            }
        };
    }

    @Override
    public void activate() {
        super.activate();
        cycleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(lockout.getPlugin(), cycleRunnable(), 0L, cycleTime);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (cycleTask != -1 && Bukkit.getScheduler().isCurrentlyRunning(cycleTask)) {
            Bukkit.getScheduler().cancelTask(cycleTask);
        }
    }

    @Override
    public BossBar bossBar() {
        return cycleBar;
    }
}
