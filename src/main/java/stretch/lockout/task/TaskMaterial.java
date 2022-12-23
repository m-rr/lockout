package stretch.lockout.task;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import stretch.lockout.task.Task;
import stretch.lockout.util.EventReflectUtil;

public class TaskMaterial extends Task {
    final private Material material;

    public TaskMaterial(Class eventClass, Material material, int value, String description) {
        super(eventClass, value, description);
        this.material = material;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        Material materialFromEvent = EventReflectUtil.getMaterialFromEvent(event);
        return super.doesAccomplish(player, event)
                && materialFromEvent != null
                && materialFromEvent == material;
    }
}
