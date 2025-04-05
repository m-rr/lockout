package stretch.lockout.reward.pattern;

import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.util.LockoutLogger;

import java.util.*;
import java.util.function.Consumer;

public class RewardChance implements RewardComponent {
    private final String description;
    private final List<WeightedReward> weightedRewards = new ArrayList<>();
    private RewardComponent selectedReward;
    private final Random random = new Random(System.currentTimeMillis());
    //private final List<Runnable> rewardRunnables = new ArrayList<>();
    private final Map<Consumer<Player>, Long> rewardRunnables = new HashMap<>();

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
            LockoutLogger.consoleLog("Chance reward did not contain any reward components.");
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
    public Map<Consumer<Player>, Long> getActions() {
        return rewardRunnables;
    }

    @Override
    public void addAction(Consumer<Player> rewardRunnable) {
        addAction(rewardRunnable, -1L);
    }

    @Override
    public void addAction(Consumer<Player> rewardRunnable, long delay) {
        rewardRunnables.put(rewardRunnable, delay);
    }

    @Override
    public void addAction(LuaValue luaRunnable) {
        addAction(luaRunnable, -1L);
    }

    @Override
    public void addAction(LuaValue luaRunnable, long delay) {
        rewardRunnables.put((player) -> luaRunnable.checkfunction().call(), delay);
    }

    public record WeightedReward(RewardComponent rewardComponent, int weight) {}
}
