package stretch.lockout.reward;

import org.luaj.vm2.LuaValue;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.util.MessageUtil;

import java.util.*;

public class RewardChance implements RewardComponent {
    private final String description;
    private final List<WeightedReward> weightedRewards = new ArrayList<>();
    private RewardComponent selectedReward;
    private final Random random = new Random(System.currentTimeMillis());
    //private final List<Runnable> rewardRunnables = new ArrayList<>();
    private final Map<Runnable, Long> rewardRunnables = new HashMap<>();

    public RewardChance(String description) {
        this.description = description;
    }

    public RewardChance(String description, List<WeightedReward> rewardComponents) {
        this.description = description;
        weightedRewards.addAll(rewardComponents);
    }

    public void determine() {
        int length = weightedRewards.size();
        if (length == 0) {
            MessageUtil.consoleLog("Chance reward did not contain any reward components.");
            return;
        }

        // order of partitions matches weightedRewards order
        int[] partitions = new int[length];
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += weightedRewards.get(i).weight;
            partitions[i] = sum;

        }

        int finalSum = sum;
        int choice = random.nextInt(finalSum);
        int index = 0;

        boolean found = false;
        while (index < length && !found) {
            if (choice < partitions[index]) {
                selectedReward = weightedRewards.get(index).rewardComponent;
                found = true;
            }
            index++;
        }
    }

    public void addReward(WeightedReward weightedReward) {
        weightedRewards.add(weightedReward);
    }

    public void addReward(RewardComponent rewardComponent, int weight) {
        weightedRewards.add(new WeightedReward(rewardComponent, weight));
    }

    // Removes all reward components matching parameter
    public void removeReward(RewardComponent rewardComponent) {
        weightedRewards.removeIf(weightedReward ->
                weightedReward.rewardComponent.hashCode() == rewardComponent.hashCode());
    }

    public void removeReward(WeightedReward weightedReward) {
        weightedRewards.remove(weightedReward);
    }

    @Override
    public void applyReward(PlayerStat playerStat) {
        if (selectedReward == null) {
            determine();
        }

        selectedReward.applyReward(playerStat);
    }

    @Override
    public RewardType getRewardType() {
        return RewardType.COMPOSITE;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<Runnable, Long> getActions() {
        return rewardRunnables;
    }

    @Override
    public void addAction(Runnable rewardRunnable) {
        addAction(rewardRunnable, -1L);
    }

    @Override
    public void addAction(Runnable rewardRunnable, long delay) {
        rewardRunnables.put(rewardRunnable, delay);
    }

    @Override
    public void addAction(LuaValue luaRunnable) {
        addAction(luaRunnable, -1L);
    }

    @Override
    public void addAction(LuaValue luaRunnable, long delay) {
        rewardRunnables.put(() -> luaRunnable.checkfunction().call(), delay);
    }

    public record WeightedReward(RewardComponent rewardComponent, int weight) {}
}
