package stretch.lockout.game.state;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.event.ReadyGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.game.CountDown;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.scoreboard.bar.LockoutTimer;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.util.MessageUtil;

import java.time.Duration;
import java.util.Optional;

public class DefaultStateHandler extends GameStateHandler {
    public DefaultStateHandler(final RaceGameContext lockout) {
        super(lockout);
    }

    @Override
    protected void preGame() {
        lockout.getTimer().setTime(Duration.ofHours(1));
        if (lockout.gameRules().contains(GameRule.AUTO_LOAD)) {
            String taskList = Optional.ofNullable(lockout.getPlugin().getConfig()
                            .getString("autoLoadTask"))
                    .orElse("default");
            lockout.getLuaEnvironment().loadFile(taskList);
        }

        // Add default teams
        lockout.getTeamManager().addDefaultTeams();

        setGameState(GameState.READY);
    }

    @Override
    protected void ready() {
        lockout.getPreGameBar().activate();
        Bukkit.getPluginManager().callEvent(new ReadyGameEvent());
    }

    @Override
    protected void starting() {
        lockout.getPreGameBar().deactivate();
        if (!lockout.getCurrentTasks().isTasksLoaded()) {
            String message = ChatColor.RED + "You can not start without loading tasks!";
            MessageUtil.consoleLog(message);
            MessageUtil.sendAllChat(message);
            setGameState(GameState.READY);
            return;
        }

        TeamManager teamManager = lockout.getTeamManager();

        // Make sure all players are on a team
        if (lockout.gameRules().contains(GameRule.FORCE_TEAM)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!teamManager.isPlayerOnTeam(player)) {
                    teamManager.createTeam(player.getName());
                    teamManager.addPlayerToTeam(player, player.getName());
                }
            });
        }

        if (lockout.gameRules().contains(GameRule.CLEAR_INV_START)) {
            teamManager.clearAllPlayerEffectAndItems();
        }

        // Set player tracker
        lockout.getPlayerTracker().setAllPlayers(teamManager.getPlayerStats());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(lockout.getPlugin(), lockout.getPlayerTracker(), 0, 3);

        MessageUtil.sendAllChat("Game Starting!");

        lockout.getGameWorld().setTime(lockout.getStartTime());

        // Call countdown
        Bukkit.getScheduler().scheduleSyncDelayedTask(lockout.getPlugin(),
                new CountDown(lockout, lockout.getCountdownTime(),
                        teamManager.getPlayerStats().stream()
                                .map(PlayerStat::getPlayer)
                                .toList()), 20);

        Bukkit.getPluginManager().callEvent(new StartGameEvent());
    }

    @Override
    protected void running() {
        TeamManager teamManager = lockout.getTeamManager();
        LockoutTimer timer = lockout.getTimer();

        teamManager.doToAllPlayers(player -> player.setInvulnerable(false));

        if (lockout.gameRules().contains(GameRule.TIMER)) {
            timer.activate();
            timer.startTimer(lockout.getPlugin(), () -> {
                if (teamManager.isTie()) {
                    setGameState(GameState.TIEBREAKER);
                }
                else {
                    Bukkit.getPluginManager().callEvent(new GameOverEvent(teamManager.getWinningTeam()));
                }
            });
        }
    }

    @Override
    protected void tiebreaker() {
        lockout.getTimer().deactivate();
        if (lockout.gameRules().contains(GameRule.TIE_BREAK) && lockout.getTieBreaker().isTasksLoaded()) {
            lockout.getTieBar().activate();
            String message = ChatColor.DARK_RED + "Tie breaker!";
            MessageUtil.sendAllActionBar(message);
            MessageUtil.sendAllChat(message);

            lockout.getTeamManager().doToAllPlayers(player ->
                    player.playSound(player, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F));
        }
        else {
            String message = ChatColor.YELLOW + "Draw";
            MessageUtil.sendAllActionBar(message);
            MessageUtil.sendAllChat(message);
            setGameState(GameState.END);
        }
    }

    @Override
    protected void endGame() {
        if (lockout.gameRules().contains(GameRule.CLEAN_INV_END)) {
            lockout.getTeamManager().clearAllPlayerEffectAndItems();
        }

        MessageUtil.sendAllChat("Game ending.");
        setGameState(GameState.PRE);
    }

    @Override
    protected void paused() {

    }
}
