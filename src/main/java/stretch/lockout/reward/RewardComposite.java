package stretch.lockout.reward;

import stretch.lockout.team.PlayerStat;

import java.util.ArrayList;
import java.util.List;

public class RewardComposite implements RewardComponent {
    private final List<RewardComponent> rewardComponents;

    public RewardComposite() {
        this.rewardComponents = new ArrayList<>();
    }

    public RewardComposite(List<RewardComponent> rewardComponents) {
        this.rewardComponents = rewardComponents;
    }

    public void addReward(RewardComponent rewardComponent) {
        rewardComponents.add(rewardComponent);
    }

    public void removeReward(RewardComponent rewardComponent) {
        rewardComponents.remove(rewardComponent);
    }

    public List<RewardComponent> getRewardComponents() {return rewardComponents;}

    @Override
    public void applyReward(PlayerStat playerStat) {
        rewardComponents.forEach(rewardComponent ->
                rewardComponent.applyReward(playerStat));
    }

    @Override
    public RewardType getRewardType() {
        return RewardType.COMPOSITE;
    }

    @Override
    public String getDescription() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < rewardComponents.size(); i++) {
            result.append(rewardComponents.get(i).getDescription());
            if (i + 1 != rewardComponents.size()) {
                result.append(" + ");
            }
        }

        return result.toString();
    }
}
