package stretch.lockout.ui.bar;

import org.bukkit.ChatColor;
import stretch.lockout.game.LockoutContext;

import java.util.List;

public class BarManager {
    private final LockoutContext lockout;
    private final LockoutTimer timer;
    private final TieBar tieBar;
    private final CycleBar cycleBar;

    public BarManager(final LockoutContext lockout) {
        this.lockout = lockout;
        this.timer = new LockoutTimer();
        this.tieBar = new TieBar();
        this.cycleBar = new CycleBar(lockout, 60, List.of(
                ChatColor.GREEN + "Right click compass to select a team!",
                ChatColor.GOLD + "LOCKOUT " + lockout.getPlugin().getDescription().getVersion()));
    }

    public LockoutTimer getTimer() {return timer;}
    public TieBar getTieBar() {return tieBar;}
    public CycleBar getPreGameBar() {return cycleBar;}

    public void destroyBars() {
        timer.deactivate();
        tieBar.deactivate();
        cycleBar.deactivate();
    }
}
