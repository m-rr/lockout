package stretch.lockout.reward.impl;

import org.bukkit.entity.Player;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.reward.base.Reward;
import stretch.lockout.task.HiddenTask;

public class RewardTask extends Reward {
    private final HiddenTask hiddenTask;

    public RewardTask(HiddenTask task, String description) {
        super(description);
        this.hiddenTask = task;
    }

    public RewardTask(HiddenTask task, RewardType rewardType, String description) {
        super(rewardType, description);
        this.hiddenTask = task;
    }

    @Override
    protected void giveReward(Player player) {
        hiddenTask.subscribe(player.getUniqueId());
    }

    public HiddenTask getDelegate() {return hiddenTask;}
}
