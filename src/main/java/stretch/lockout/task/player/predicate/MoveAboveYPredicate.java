package stretch.lockout.task.player.predicate;

import org.bukkit.Location;

import java.util.function.Predicate;

public class MoveAboveYPredicate implements Predicate<Location> {
    final private double targetY;
    public MoveAboveYPredicate(double y) {
        this.targetY = y;
    }

    @Override
    public boolean test(Location location) {
        return location.getY() >= targetY;
    }
}
