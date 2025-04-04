package stretch.lockout.listener;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import stretch.lockout.event.*;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.platform.Platform;
import stretch.lockout.task.HiddenTask;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.JsonUtil;
import stretch.lockout.util.LockoutLogger;

import java.util.function.Predicate;

public class TaskRaceEventHandler implements Listener {
    private final LockoutContext lockout;
    public TaskRaceEventHandler(final LockoutContext taskRaceContext) {
        this.lockout = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onTaskCompleted(TaskCompletedEvent taskCompletedEvent) {
        if (taskCompletedEvent.isCancelled()) {
            return;
        }

        var task = taskCompletedEvent.getTask();
        var scoredPlayerStat = task.getScoredPlayer();
        TeamManager teamManager = lockout.getTeamManager();
        LockoutTeam team = taskCompletedEvent.getPlayer().getTeam();

        // apply rewards
        if (task.hasReward() && lockout.settings().hasRule(LockoutGameRule.ALLOW_REWARD)) {
            var reward = task.getReward();
            reward.applyReward(scoredPlayerStat);
            if (!(task instanceof HiddenTask)) {
                var rewardEvent = new RewardApplyEvent(scoredPlayerStat, reward);
                Bukkit.getPluginManager().callEvent(rewardEvent);
            }

            var actions = reward.getActions();
            if (!actions.isEmpty()) {
                lockout.getRewardScheduler().scheduleRewardActions(reward);
            }
        }

        LockoutTeam winningTeam = teamManager.getWinningTeam();

        // TODO Add maxscore setting properly.
        Predicate<LockoutContext> isGameOver = lockout.settings().hasRule(LockoutGameRule.MAX_SCORE) ?
                (game) -> game.settings().getMaxScore() > 0 && team.getScore() >= game.settings().getMaxScore() :
                (game) -> (long) game.getTeamManager().getTeams().size() > 1 && teamManager.getOpposingTeams(winningTeam).stream()
                        .noneMatch(teams -> game.getCurrentTaskCollection().remainingPoints() + teams.getScore() >= winningTeam.getScore());

        if (isGameOver.test(lockout)) {
          Bukkit.getPluginManager().callEvent(new GameOverEvent(winningTeam));
          return;
        }

    }

    @EventHandler
    public void onStartGame(StartGameEvent startGameEvent) {
        lockout.getTeamManager().getTeams()
                .removeIf(lockoutTeam -> lockoutTeam.playerCount() < 1);
    }

    @EventHandler
    public void onGameOver(GameOverEvent gameOverEvent) {
        if (gameOverEvent.isCancelled()) {
            return;
        }

        lockout.getGameStateHandler().setGameState(GameState.PAUSED);

        // TODO io
        if (!lockout.settings().hasRule(LockoutGameRule.DEV)) {
            JsonObject report = JsonUtil.generateReport(lockout);
            Bukkit.getScheduler().runTaskAsynchronously(lockout.getPlugin(), () -> Platform.collectReport(report));
        }


        // TODO this should be in state handler
        Bukkit.getScheduler().scheduleSyncDelayedTask(lockout.getPlugin(),
                () -> lockout.getGameStateHandler().setGameState(GameState.END), 100);

        LockoutLogger.debugLog(lockout.settings(), "Game has ended");
    }

    @EventHandler
    public void onResetGame(ResetGameEvent resetGameEvent) {
        lockout.getGameStateHandler().setGameState(GameState.END);
    }

}
