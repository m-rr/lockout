package stretch.lockout.task.player;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import stretch.lockout.task.Task;
import stretch.lockout.util.PotionUtil;

public class TaskPotion extends Task {
    private final PotionEffectType potionEffectType;
    public TaskPotion(PotionEffectType potionEffectType, int value, String description) {
        super(EntityPotionEffectEvent.class, value, description);
        this.potionEffectType = potionEffectType;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        var potionEffect = PotionUtil.getPotionEffectFromEvent(event);
        return potionEffect.getType().getName().equals(potionEffectType.getName())
                && super.doesAccomplish(player, event);
    }
}
