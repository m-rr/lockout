package stretch.lockout.task;

import stretch.lockout.task.player.TaskArmorStand;
import stretch.lockout.task.player.TaskDamageFromSource;
import stretch.lockout.task.player.TaskMove;
import stretch.lockout.task.player.TaskPotion;
import stretch.lockout.task.structure.TaskStructure;

public class TaskBuilder {
    private TaskComponent task;
    public TaskBuilder(TaskType type) {
        this.task = newTask(type);
    }

    private TaskComponent newTask(TaskType type) {
        return switch(type) {
            case MOB -> new TaskMob(null, null, 0, "");
            case MOVE -> new TaskMove(null, null, 0, "");
            case QUEST -> new Task(null, 0, "");
            case POTION -> new TaskPotion(null, 0, "");
            case MATERIAL -> new TaskMaterial(null, null, 0, "");
            case STRUCTURE -> new TaskStructure(null, null, 0, "");
            case ARMOR_STAND -> new TaskArmorStand(null, 0, null, "");
            case DAMAGE_FROM_SOURCE -> new TaskDamageFromSource(null, null, 0, "");
        };
    }


}
