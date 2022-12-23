package stretch.lockout.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;

public class EventReflectUtil {


    public static Location getLocationFromEvent(Event event) {
        Method getLocation = null;
        try {
            getLocation = event.getClass().getMethod("getTo");
            return (Location) getLocation.invoke(event);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }
        return null;
    }

    private static Object getMaterialMethodFromEventHelper(Object event, Class searchClass) {

        Queue<Object> objectReturns = new ArrayDeque<>();
        objectReturns.add(event);
        Object curr;
        while(objectReturns.size() > 0) {
            curr = objectReturns.remove();
            if (curr.getClass() == searchClass) {
                return curr;
            }
            else {
                for (var method : curr.getClass().getMethods()) {
                    if (method.getParameterCount() == 0) {
                        try {
                            var result = method.invoke(curr);
                            if (result.getClass() == searchClass) {
                                return result;
                            }
                            objectReturns.add(result);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    // Returns null on failure.
    public static Material getMaterialFromEventTest(Event event) {
        return (Material) getMaterialMethodFromEventHelper(event, Material.class);
    }

    /*
    * This is the big ugly.
    * */
    public static Material getMaterialFromEvent(Event event) {

        Material result = null;
        Object penultimateObject = null;
        Method getBlockOrItem = null;

        if (getBlockOrItem == null) {
            try {
                getBlockOrItem = event.getClass().getMethod("getItem");
                var item = (Item) getBlockOrItem.invoke(event);
                return item.getItemStack().getType();
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  | ClassCastException e) {
                if (e instanceof ClassCastException) {
                    getBlockOrItem = null;
                }
            }
        }

        if (getBlockOrItem == null ) {
            try {
                getBlockOrItem = event.getClass().getMethod("getItem");
                var itemStack = (ItemStack) getBlockOrItem.invoke(event);
                return itemStack.getType();
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {

            }
        }

        if (getBlockOrItem == null) {
            try {
                getBlockOrItem = event.getClass().getMethod("getHitBlock");
                var block = (Block) getBlockOrItem.invoke(event);
                return block.getType();
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

            }
        }

        if (getBlockOrItem == null) {
            try {
                getBlockOrItem = event.getClass().getMethod("getItemDrop");
                var item = (Item) getBlockOrItem.invoke(event);
                return item.getItemStack().getType();
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

            }
        }

        if (getBlockOrItem == null) {
            try {
                getBlockOrItem = event.getClass().getMethod("getDamager");
                var block = (Block) getBlockOrItem.invoke(event);
                return block.getType();
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

            }
        }

        if (getBlockOrItem == null) {
            try {
                getBlockOrItem = event.getClass().getMethod("getItemType");
                return (Material) getBlockOrItem.invoke(event);
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

            }
        }

        try {
            getBlockOrItem = event.getClass().getMethod("getBlock");
            var block = (Block) getBlockOrItem.invoke(event);
            return block.getType();
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }

        try {
            getBlockOrItem = event.getClass().getMethod("getCurrentItem");
            var item = (ItemStack) getBlockOrItem.invoke(event);
            return item.getType();
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }

        // get either block or item from first method.
        try {
            if (getBlockOrItem != null) {
                penultimateObject = getBlockOrItem.invoke(event);
            }
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }

        Method getMaterial = null;
        if (penultimateObject != null) {
            try {
                getMaterial = penultimateObject.getClass().getMethod("getType");
            }
            catch (NoSuchMethodException ignored) {

            }
        }

        try {
            if (getMaterial != null) {
                result = (Material) getMaterial.invoke(penultimateObject);
            }
        }
        catch (IllegalAccessException | InvocationTargetException ignored) {

        }

        return result;
    }

    // Returns entity from generic event or returns null if there is none
    public static @Nullable Entity getEntityFromEvent(Event event) {
        Entity result = null;
        Method getEntity = null;
        // EntityCombustByEntityEvent
        try {
            getEntity = event.getClass().getMethod("getCombuster");
        }
        catch (NoSuchMethodException ignored) {

        }
        if (getEntity == null) {
            try {
                getEntity = event.getClass().getMethod("getEntity");
            }
            catch (NoSuchMethodException ignored) {
        }

        }
        if (getEntity == null) {
            try {
                getEntity = event.getClass().getMethod("getRightClicked");
            }
            catch (NoSuchMethodException ignored) {

            }
        }
        try {
            if (getEntity != null) {
                result = (Entity) getEntity.invoke(event);
            }
        }
        catch (IllegalAccessException | InvocationTargetException ignored) {

        }

        return result;
    }
}
