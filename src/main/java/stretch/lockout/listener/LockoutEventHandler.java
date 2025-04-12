package stretch.lockout.listener;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.event.ResetGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.platform.Platform;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.JsonUtil;
import stretch.lockout.util.LockoutLogger;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Handles {@link TaskCompletedEvent}, {@link GameOverEvent}, {@link ResetGameEvent} and {@link StartGameEvent}.
 *
 * @author m-rr
 * @version @projectVersion@
 * @since 2.5.1
 * */
public class LockoutEventHandler implements Listener {
    private final LockoutContext lockout;
    public LockoutEventHandler(@NonNull final LockoutContext lockoutContext) {
        this.lockout = Objects.requireNonNull(lockoutContext, "LockoutContext cannot be null");
        Bukkit.getPluginManager().registerEvents(this, lockoutContext.getPlugin());
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
            lockout.getRewardScheduler()
                    .scheduleReward(scoredPlayerStat, task);
        }

        LockoutTeam winningTeam = teamManager.getWinningTeam();

        // TODO Add max score setting properly.
        Predicate<LockoutContext> isGameOver = lockout.settings().hasRule(LockoutGameRule.MAX_SCORE) ?
                (game) -> game.settings().getMaxScore() > 0 && team.getScore() >= game.settings().getMaxScore() :
                (game) -> (long) game.getTeamManager().getTeams().size() > 1 && teamManager.getOpposingTeams(winningTeam).stream()
                        .noneMatch(teams -> game.getTaskManager().getRemainingPoints() + teams.getScore() >= winningTeam.getScore());

        if (isGameOver.test(lockout)) {
          Bukkit.getPluginManager().callEvent(new GameOverEvent(winningTeam));
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

        LockoutLogger.debugLog("Game has ended");
    }

    @EventHandler
    public void onResetGame(ResetGameEvent resetGameEvent) {
        lockout.getGameStateHandler().setGameState(GameState.END);
    }

}
