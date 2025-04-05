package stretch.lockout.reward.api;

import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;
import stretch.lockout.team.player.PlayerStat;

import java.util.Map;
import java.util.function.Consumer;

public interface RewardComponent {
    void applyReward(PlayerStat playerStat);
    RewardType getRewardType();
    String getDescription();
    Map<Consumer<Player>, Long> getActions();
    void addAction(Consumer<Player> rewardRunnable);
    void addAction(LuaValue luaRunnable);
    void addAction(Consumer<Player> rewardRunnable, long delay);
    void addAction(LuaValue luaRunnable, long delay);
}
