package stretch.lockout.scoreboard.bar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;

public abstract class LockoutBar{
    public void activate() {
        Bukkit.getOnlinePlayers().forEach(player -> bossBar().addPlayer(player));
    };
    public void deactivate() {
        bossBar().removeAll();
    };

    public abstract BossBar bossBar();
}
