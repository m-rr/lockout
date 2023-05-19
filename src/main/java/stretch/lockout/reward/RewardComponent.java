package stretch.lockout.reward;

import stretch.lockout.team.PlayerStat;

import java.util.List;

public interface RewardComponent {
    void applyReward(PlayerStat playerStat);
    RewardType getRewardType();
    String getDescription();
    List<Runnable> getActions();
    void addAction(Runnable rewardRunnable);
}
