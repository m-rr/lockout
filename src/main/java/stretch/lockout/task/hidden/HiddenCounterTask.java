package stretch.lockout.task.hidden;

import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.team.player.PlayerStat;

import java.util.HashMap;
import java.util.Map;

public class HiddenCounterTask extends HiddenTask {
    private int value = 0;
    private final Map<PlayerStat, Integer> playerCompletions = new HashMap<>();
    public HiddenCounterTask(TaskComponent taskComponent) {
        super(taskComponent);
    }

    @Override
    public TaskComponent setValue(int value) {
        this.value = value;
        return this;
    }

    public int getCounterValue() {
        return value;
    }

    /** No reward should ever be applied for completing a secondary objective */
    @Override
    public boolean hasReward() {
        return false;
    }

    @Override
    public void setCompletedBy(PlayerStat playerStat) {
        super.setCompletedBy(playerStat);
        // Update counter
        playerCompletions.compute(playerStat, (k, v) -> v == null ? 1 : v + 1);
    }

    /** @return completions * value per player */
    public Map<PlayerStat, Integer> getPlayerCompletionValues() {
        Map<PlayerStat, Integer> result = new HashMap<>();
        for (Map.Entry<PlayerStat, Integer> entry : playerCompletions.entrySet()) {
            Integer newValue = entry.getValue() * this.value;
            result.put(entry.getKey(), newValue);
        }

        return result;
    }
}
