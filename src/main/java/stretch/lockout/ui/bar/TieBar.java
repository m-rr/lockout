package stretch.lockout.ui.bar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class TieBar extends LockoutBar {
    private final BossBar tieBar =
            Bukkit.createBossBar(ChatColor.GOLD + "TIEBREAKER", BarColor.RED, BarStyle.SOLID);

    @Override
    public BossBar bossBar() {
        return tieBar;
    }
}
