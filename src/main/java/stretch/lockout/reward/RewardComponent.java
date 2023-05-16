package stretch.lockout.reward;

import stretch.lockout.team.PlayerStat;

public interface RewardComponent {
    void applyReward(PlayerStat playerStat);
    RewardType getRewardType();
    String getDescription();
}
