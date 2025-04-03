package stretch.lockout.task.special;

import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import stretch.lockout.task.Task;
import stretch.lockout.event.executor.LockoutWrappedEvent;

import java.util.Optional;

public class TaskAdvancement extends Task {
    final String targetAdvancement;

    public TaskAdvancement(final String advancementTitle, int value, String description) {
        super(PlayerAdvancementDoneEvent.class, value, description);
        this.targetAdvancement = advancementTitle.toLowerCase();
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        if (!(lockoutEvent.getEvent() instanceof PlayerAdvancementDoneEvent advancementEvent)) {
            return false;
        }

        Optional<AdvancementDisplay> maybeAdvancement = Optional.ofNullable(advancementEvent.getAdvancement().getDisplay());
        return maybeAdvancement.map(advancementDisplay -> advancementDisplay.displayName().toString().equalsIgnoreCase(targetAdvancement)).orElse(false);
    }
}
