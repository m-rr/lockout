package stretch.lockout.reward.pattern;

import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.team.player.PlayerStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RewardComposite implements RewardComponent {
    private final List<RewardComponent> rewardComponents;
    private final Map<Consumer<Player>, Long> rewardRunnables = new HashMap<>();

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
    public Map<Consumer<Player>, Long> getActions() {return rewardRunnables;}

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
            String rewardDescription = rewardComponents.get(i).getDescription();
            if (!rewardDescription.isEmpty()) {
                result.append(rewardDescription);
                if (i + 1 != rewardComponents.size() && !rewardComponents.get(i + 1).getDescription().isEmpty()) {
                    result.append(" + ");
                }
            }

        }

        return result.toString();
    }
}
