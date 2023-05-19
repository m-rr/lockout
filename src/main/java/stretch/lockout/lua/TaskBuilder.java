package stretch.lockout.lua;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.TaskMaterial;
import stretch.lockout.task.TaskMob;

public class TaskBuilder {

    public static TaskComponent createTaskMaterial(Class eventClass, Varargs args) {
        Material material = (Material) CoerceLuaToJava.coerce(args.arg(1), Material.class);
        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
        ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

        return new TaskMaterial(eventClass, material, value, description)
                .setGuiItemStack(guiItem);

    }

    public static TaskComponent createTaskMob(Class eventClass, Varargs args) {
        EntityType entityType = (EntityType) CoerceLuaToJava.coerce(args.arg(1), EntityType.class);
        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
        ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

        return new TaskMob(eventClass, entityType, value, description)
                .setGuiItemStack(guiItem);
    }
}
