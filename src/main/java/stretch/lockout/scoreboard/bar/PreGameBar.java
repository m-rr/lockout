package stretch.lockout.scoreboard.bar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class PreGameBar extends LockoutBar {
    private final BossBar preGameBar =
            Bukkit.createBossBar(ChatColor.GREEN + "RIGHT CLICK COMPASS TO SELECT TEAM", BarColor.WHITE, BarStyle.SOLID);
    @Override
    public BossBar bossBar() {
        return preGameBar;
    }
}
