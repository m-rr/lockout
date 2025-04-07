package stretch.lockout.lua;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.composite.TaskChoice;
import stretch.lockout.task.composite.TaskSequence;
import stretch.lockout.task.composite.TaskSet;
import stretch.lockout.task.impl.block.TaskMaterial;
import stretch.lockout.task.impl.entity.TaskMob;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LuaTaskBuilder {

    public static TaskComponent createTaskMaterial(Varargs args, Class eventClass) {
        Material material = (Material) CoerceLuaToJava.coerce(args.arg(1), Material.class);
        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
        ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

        return new TaskMaterial(eventClass, material, value, description)
                .setGuiItemStack(guiItem);

    }

    public static TaskComponent createTaskEntity(Varargs args, Class eventClass) {
        EntityType entityType = (EntityType) CoerceLuaToJava.coerce(args.arg(1), EntityType.class);
        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
        ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

        return new TaskMob(eventClass, entityType, value, description)
                .setGuiItemStack(guiItem);
    }


    public static TaskComponent createComposite(Varargs args, Class clazz) {
        ItemStack guiItem = new ItemStack ((Material) CoerceLuaToJava.coerce(args.arg(1), Material.class));
        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);

        List<TaskComponent> tasks = new ArrayList<>();
        int i = 4;
        LuaValue curr = args.arg(i);
        while (!curr.isnil()) {
            tasks.add((TaskComponent) CoerceLuaToJava.coerce(curr, TaskComponent.class));
            curr = args.arg(++i);
        }

        return switch (clazz.getSimpleName()) {
            case "TaskSequence" -> new TaskSequence(tasks, value, description).setGuiItemStack(guiItem);
            case "TaskSet" -> new TaskSet(tasks, value, description).setGuiItemStack(guiItem);
            case "TaskChoice" -> new TaskChoice(tasks, value, description).setGuiItemStack(guiItem);
            default -> throw new IllegalStateException("Unexpected value: " + clazz.getName());
        };
    }

    public static TaskComponent createGroupTaskMaterial(Varargs args, Class eventClass) {
        Set<Material> materials = (ImmutableSet<Material>) CoerceLuaToJava.coerce(args.arg(1), ImmutableSet.class);
        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
        ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

        List<TaskComponent> tasks = materials.stream()
                .map(m -> (TaskComponent) new TaskMaterial(eventClass, m, value, description))
                .toList();

        return new TaskChoice(tasks, value, description)
                .setGuiItemStack(guiItem);
    }

    public static TaskComponent createGroupTaskEntity(Varargs args, Class eventClass) {
        Set<EntityType> entities = (ImmutableSet<EntityType>) CoerceLuaToJava.coerce(args.arg(1), ImmutableSet.class);

        int value = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
        String description = (String) CoerceLuaToJava.coerce(args.arg(3), String.class);
        ItemStack guiItem = new ItemStack((Material) CoerceLuaToJava.coerce(args.arg(4), Material.class));

        List<TaskComponent> tasks = entities.stream()
                .map(m -> (TaskComponent) new TaskMob(eventClass, m, value, description))
                .toList();

        return new TaskChoice(tasks, value, description)
                .setGuiItemStack(guiItem);
    }

}
