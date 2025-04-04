package stretch.lockout.task.api;

import org.bukkit.Location;

import java.time.Duration;

public interface TimeCompletableTask extends TaskComponent {
    void setTimeCompleted(Duration time);

    Duration getTimeCompleted();

    void setLocation(Location loc);

    Location getLocation();
}
