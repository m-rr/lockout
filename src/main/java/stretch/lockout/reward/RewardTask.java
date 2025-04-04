package stretch.lockout.reward;

import org.bukkit.entity.Player;
import stretch.lockout.task.HiddenTask;

public class RewardTask extends RewardLeaf {
    private final HiddenTask taskInvisible;

    public RewardTask(HiddenTask task, String description) {
        super(description);
        this.taskInvisible = task;
    }

    public RewardTask(HiddenTask task, RewardType rewardType, String description) {
        super(rewardType, description);
        this.taskInvisible = task;
    }

    @Override
    protected void giveReward(Player player) {
        taskInvisible.subscribe(player);
    }

    public HiddenTask getDelegate() {return taskInvisible;}
}
