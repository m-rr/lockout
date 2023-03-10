package stretch.lockout.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import stretch.lockout.team.LockoutTeam;

import java.util.HashSet;
import java.util.Set;

public class ScoreboardHandler {
    private Set<LockoutTeam> teams = new HashSet<>();
    private Scoreboard board;
    private Objective boardObjective;
    private final String TITLE = ChatColor.BLUE + "LOCKOUT";

    public ScoreboardHandler() {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        boardObjective = board.registerNewObjective("teamscore", "score");
        boardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        boardObjective.setDisplayName(TITLE);
    }

    public void addTeam(LockoutTeam lockoutTeam) {
        teams.add(lockoutTeam);
    }

    public void update() {
        teams.forEach(team -> {
            Score teamScore = boardObjective.getScore(ChatColor.GOLD + team.getName());
            teamScore.setScore(team.getScore());
            team.doToPlayers(player -> player.setScoreboard(board));
        });
    }

    public void resetScoreboard() {
        teams.forEach(team -> {
            team.doToPlayers(player -> player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
        });
    }

    public Scoreboard getBoard() {return board;}
}
