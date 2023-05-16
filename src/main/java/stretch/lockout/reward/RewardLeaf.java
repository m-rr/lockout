package stretch.lockout.reward;

import org.bukkit.entity.Player;
import stretch.lockout.team.PlayerStat;

public abstract class RewardLeaf implements RewardComponent {
    final protected RewardType rewardType;
    final protected String description;
    public RewardLeaf(String description) {
        this.description = description;
        this.rewardType = RewardType.POSITIVE;
    }
    public RewardLeaf(RewardType rewardType, String description) {
        this.description = description;
        this.rewardType = rewardType;
    }

    @Override
    public void applyReward(PlayerStat playerStat) {
        switch (rewardType) {
            case POSITIVE -> giveReward(playerStat.getPlayer());
            //case TEAM_POSITIVE -> playerStat.getTeam().getPlayerStats().stream()
            //        .map(PlayerStat::getPlayer).forEach(this::giveReward);
            case TEAM_POSITIVE -> playerStat.getTeam().doToPlayers(this::giveReward);
            case ENEMY_NEGATIVE -> playerStat.getTeam().doToOpposingTeams(this::giveReward);
        }
    }

    protected abstract void giveReward(Player player);

    @Override
    public RewardType getRewardType() {return rewardType;}
    @Override
    public String getDescription() {return description;}
}
