package stretch.lockout.reward;

import org.luaj.vm2.LuaValue;
import stretch.lockout.team.PlayerStat;

import java.util.Map;

public interface RewardComponent {
    void applyReward(PlayerStat playerStat);
    RewardType getRewardType();
    String getDescription();
    Map<Runnable, Long> getActions();
    void addAction(Runnable rewardRunnable);
    void addAction(LuaValue luaRunnable);
    void addAction(Runnable rewardRunnable, long delay);
    void addAction(LuaValue luaRunnable, long delay);
}
