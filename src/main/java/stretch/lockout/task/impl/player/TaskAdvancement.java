package stretch.lockout.task.impl.player;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.base.Task;

import java.util.Optional;


public class TaskAdvancement extends Task {
    final String targetAdvancementTitle;

    public TaskAdvancement(final String advancementTitle, int value, String description) {
        super(PlayerAdvancementDoneEvent.class, value, description);
        // Store the target title exactly as the user would expect it (without brackets)
        this.targetAdvancementTitle = advancementTitle != null ? advancementTitle.trim() : "";
        if (this.targetAdvancementTitle.isEmpty()) {
            //plugin.getLogger().warning("TaskAdvancement created with empty target title for description: " + description);
        }
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        if (!(lockoutEvent.getEvent() instanceof PlayerAdvancementDoneEvent advancementEvent)) {
            return false;
        }
        if (!super.doesAccomplish(lockoutEvent)) {
            return false;
        }

        Optional<AdvancementDisplay> maybeDisplay = Optional.ofNullable(advancementEvent.getAdvancement().getDisplay());

        return maybeDisplay.map(display -> {
            Component displayNameComponent = display.displayName();
            String serializedText = PlainTextComponentSerializer.plainText()
                    .serialize(displayNameComponent)
                    .trim(); // Trim whitespace just in case

            String textToCompare = serializedText;

            // Check for and strip surrounding brackets ---
            if (serializedText.length() >= 2 && serializedText.startsWith("[") && serializedText.endsWith("]")) {
                textToCompare = serializedText.substring(1, serializedText.length() - 1);
            }

            // Perform case-insensitive comparison on the potentially stripped text
            return textToCompare.equalsIgnoreCase(this.targetAdvancementTitle);

        }).orElse(false);
    }
}
