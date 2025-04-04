package stretch.lockout.task.impl.player;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.base.Task;

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

        //Optional<AdvancementDisplay> maybeAdvancement = Optional.ofNullable(advancementEvent.getAdvancement().getDisplay());
        //return maybeAdvancement.map(advancementDisplay -> advancementDisplay.displayName().toString().equalsIgnoreCase(targetAdvancement)).orElse(false);

        //boolean success = SpigotSafeCall.callUnsafeSpigotMethod(
                //() -> {MessageUtil.consoleLog(advancementEvent.getAdvancement().getDisplay().displayName().examinableName()); return true;},
                //false);

        return advancementEvent.getAdvancement().displayName().toString().equalsIgnoreCase(targetAdvancement);
    }
}
