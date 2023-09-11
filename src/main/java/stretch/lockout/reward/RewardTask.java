package stretch.lockout.reward;

import org.bukkit.entity.Player;
import stretch.lockout.task.TaskInvisible;

public class RewardTask extends RewardLeaf {
    private final TaskInvisible taskInvisible;

    public RewardTask(TaskInvisible task, String description) {
        super(description);
        this.taskInvisible = task;
    }

    public RewardTask(TaskInvisible task, RewardType rewardType, String description) {
        super(rewardType, description);
        this.taskInvisible = task;
    }

    @Override
    protected void giveReward(Player player) {
        taskInvisible.subscribe(player);
    }

    public TaskInvisible getDelegate() {return taskInvisible;}
}
