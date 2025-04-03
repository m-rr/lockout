package stretch.lockout.task.special;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.Task;
import stretch.lockout.util.PotionUtil;

import java.util.Optional;

public class TaskPotion extends Task {
    //private final PotionEffectType potionEffectType;
    private final NamespacedKey potionKey;
    public TaskPotion(PotionEffectType potionEffectType, int value, String description) {
        super(EntityPotionEffectEvent.class, value, description);
        //this.potionEffectType = potionEffectType;
        this.potionKey = potionEffectType.getKey();
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<PotionEffect> optionalPotionEffect = lockoutEvent.getPotionEffect();
        if (optionalPotionEffect.isEmpty()) {
            return false;
        }
        PotionEffect potionEffect = optionalPotionEffect.get();

        //return potionEffect.getType().getName().equals(potionEffectType.getName())
        //        && super.doesAccomplish(player, event);
        return potionEffect.getType().getKey() == potionKey
                && super.doesAccomplish(lockoutEvent);
    }
}
