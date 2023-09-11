package stretch.lockout.reward;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import stretch.lockout.team.PlayerStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardComposite implements RewardComponent {
    private final List<RewardComponent> rewardComponents;
    //private final List<Runnable> rewardRunnables = new ArrayList<>();
    private final Map<Runnable, Long> rewardRunnables = new HashMap<>();

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
    public Map<Runnable, Long> getActions() {return rewardRunnables;}

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
