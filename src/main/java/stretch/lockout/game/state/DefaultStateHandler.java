package stretch.lockout.game.state;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.event.ReadyGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.event.TieBreakerEvent;
import stretch.lockout.event.state.PlayerStateChangeTask;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.ui.bar.LockoutTimer;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;

import java.time.Duration;
import java.util.Optional;

public class DefaultStateHandler extends GameStateHandler {
    public DefaultStateHandler(final LockoutContext lockout) {
        super(lockout);
    }

    @Override
    protected void preGame() {
        int time = lockout.settings().hasRule(LockoutGameRule.TIMER)
            ? lockout.settings().getTimerSeconds()
            : LockoutSettings.DEFAULT_TIMER_SECONDS;
        lockout.getUiManager().getTimer()
            .setTime(Duration.ofSeconds(time));

        // TODO make method for this
        if (lockout.settings().hasRule(LockoutGameRule.AUTO_LOAD)) {
            String boardName = Optional.ofNullable(lockout.getPlugin().getConfig()
                            .getString("autoLoadTask"))
                    .orElse("default");
            
            lockout.getBoardManager().loadBoard(boardName);
        }

        lockout.getEventExecutor().register();
        lockout.getTeamManager().addDefaultTeams();

        setGameState(GameState.READY);
    }

    @Override
    protected void ready() {
        Bukkit.getPluginManager().callEvent(new ReadyGameEvent());
    }

    @Override
    protected void starting() {

        if (!lockout.getCurrentTaskCollection().isTasksLoaded()) {
            String message = ChatColor.RED + "You can not start without loading tasks!";
            MessageUtil.consoleLog(message);
            MessageUtil.sendAllChat(message);
            setGameState(GameState.READY);
            return;
        }
        TeamManager teamManager = lockout.getTeamManager();

        // Make sure all players are on a team
        if (lockout.settings().hasRule(LockoutGameRule.FORCE_TEAM)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!teamManager.isPlayerOnTeam(player)) {
                    teamManager.createTeam(player.getName());
                    teamManager.addPlayerToTeam(player, player.getName());
                }
            });
        }

        // Prevent players from joining a team late
        teamManager.lock();

        if (lockout.settings().hasRule(LockoutGameRule.CLEAR_INV_START)) {
            teamManager.clearAllPlayerEffectAndItems();
        }

        if (lockout.settings().hasRule(LockoutGameRule.START_SPAWN)) {
            teamManager.doToAllPlayers(player -> player.teleport(lockout.settings().getGameWorld().getSpawnLocation()));
        }

        if (lockout.settings().hasRule(LockoutGameRule.REVOKE_ADVANCEMENT)) {
            teamManager.doToAllPlayers(player -> {
                Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
                    var progress = player.getAdvancementProgress(advancement);
                    progress.getAwardedCriteria().forEach(progress::revokeCriteria);
                });
            });
        }

        // Set player tracker
        lockout.getPlayerTracker().setAllPlayers(teamManager.getPlayerStats());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(lockout.getPlugin(), lockout.getPlayerTracker(), 0, 3);

        lockout.settings().getGameWorld().setTime(lockout.settings().getStartTime());

        MessageUtil.sendAllChat("Game Starting!");

        Bukkit.getPluginManager().callEvent(new StartGameEvent());
    }

    @Override
    protected void running() {
        TeamManager teamManager = lockout.getTeamManager();
        LockoutTimer timer = lockout.getUiManager().getTimer();

        teamManager.doToAllPlayers(player -> player.setInvulnerable(false));

        Runnable timerCallback;
        if (lockout.settings().hasRule(LockoutGameRule.TIMER)) {
            timer.activate();
            timerCallback = () -> {
                if (teamManager.isTie()) {
                    setGameState(GameState.TIEBREAKER);
                }
                else {
                    Bukkit.getPluginManager().callEvent(new GameOverEvent(teamManager.getWinningTeam()));
                }
            };
        }
        else {
            timer.activate();
            timerCallback = () -> setGameState(GameState.END);
        }

        timer.startTimer(lockout.getPlugin(),
                         lockout.settings().hasRule(LockoutGameRule.TIMER),
                         timerCallback);

        PlayerStateChangeTask playerStateChecker = new PlayerStateChangeTask(lockout);
        playerStateChecker.runTaskTimer(lockout.getPlugin(), 0L, lockout.settings().getPlayerUpdateTicks());
    }

    @Override
    protected void tiebreaker() {
        Bukkit.getPluginManager().callEvent(new TieBreakerEvent());
        if (!lockout.settings().hasRule(LockoutGameRule.TIE_BREAK) || !lockout.getTieBreaker().isTasksLoaded()) {
            setGameState(GameState.END);
        }
    }

    @Override
    protected void endGame() {
        if (lockout.settings().hasRule(LockoutGameRule.CLEAN_INV_END)) {
            lockout.getTeamManager().clearAllPlayerEffectAndItems();
        }

        lockout.getEventExecutor().unregister();
        //lockout.getLuaEnvironment().resetTables();
        //lockout.getLuaEnvironment().resetRequiredFiles();

        MessageUtil.sendAllChat("Game ending.");
        setGameState(GameState.PRE);
    }

    @Override
    protected void paused() {

    }
}
