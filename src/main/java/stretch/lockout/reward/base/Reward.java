package stretch.lockout.reward.base;

import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.team.player.PlayerStat;

import java.util.*;
import java.util.function.Consumer;

public abstract class Reward implements RewardComponent {
    final protected RewardType rewardType;
    final protected String description;
    private final Map<Consumer<Player>, Long> actions = new HashMap<>();
    public Reward(String description) {
        this.description = description;
        this.rewardType = RewardType.SOLO;
    }
    public Reward(RewardType rewardType, String description) {
        this.description = description;
        this.rewardType = rewardType;
    }

    @Override
    public Map<Consumer<Player>, Long> getActions() {
        return actions;
    }

    @Override
    public void addAction(Consumer<Player> rewardRunnable) {
        addAction(rewardRunnable, -1L);
    }

    @Override
    public void addAction(Consumer<Player> rewardRunnable, long delay) {
        actions.put(rewardRunnable, delay);
    }

    @Override
    public void addAction(LuaValue luaRunnable) {
        addAction(luaRunnable, -1L);
    }

    @Override
    public void addAction(LuaValue luaRunnable, long delay) {
        actions.put((player) -> luaRunnable.checkfunction().call(), delay);
    }

    @Override
    public void applyReward(PlayerStat playerStat) {
        switch (rewardType) {
            case SOLO -> giveReward(playerStat.getPlayer());
            case TEAM -> playerStat.getTeam().doToPlayers(this::giveReward);
            case ENEMY -> playerStat.getTeam().doToOpposingTeams(this::giveReward);
        }
    }

    protected abstract void giveReward(Player player);

    @Override
    public RewardType getRewardType() {return rewardType;}
    @Override
    public String getDescription() {return description;}
}
