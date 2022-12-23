package stretch.lockout.reward;

import stretch.lockout.team.PlayerStat;

public interface RewardComponent {
    void applyReward(PlayerStat playerStat);
    //void applyReward(Player player);
    RewardType getRewardType();
    String getDescription();
}
