package stretch.lockout.util;

import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PotionUtil {

    public static PotionEffect getPotionEffectFromEvent(Event event) {

        PotionEffect result = null;
        Method getPotionEffect = null;

        try {
            getPotionEffect = event.getClass().getMethod("getNewEffect");
        }
        catch (NoSuchMethodException ignored) {

        }
        if (getPotionEffect != null) {
            try {
                result = (PotionEffect) getPotionEffect.invoke(event);
            } catch (IllegalAccessException | InvocationTargetException ignored) {

            }
        }

        return result;
    }
}
