package stretch.lockout.reward;

import org.bukkit.entity.Player;
import org.luaj.vm2.LuaValue;
import stretch.lockout.team.player.PlayerStat;

import java.util.*;

public abstract class RewardLeaf implements RewardComponent {
    final protected RewardType rewardType;
    final protected String description;
    //private final List<Runnable> rewardRunnables = new ArrayList<>();
    private final Map<Runnable, Long> rewardRunnables = new HashMap<>();
    public RewardLeaf(String description) {
        this.description = description;
        this.rewardType = RewardType.SOLO;
    }
    public RewardLeaf(RewardType rewardType, String description) {
        this.description = description;
        this.rewardType = rewardType;
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
