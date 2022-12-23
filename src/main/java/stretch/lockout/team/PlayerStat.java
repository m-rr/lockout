package stretch.lockout.team;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import stretch.lockout.task.TaskComponent;

import java.util.HashSet;

public class PlayerStat {
    private Player player;
    final private HashSet<TaskComponent> completedTasks = new HashSet<>();
    private LockoutTeam lockoutTeam;
    private int score = 0;
    private Scoreboard board;

    public PlayerStat(Player player, LockoutTeam team) {
        this.player = player;
        this.lockoutTeam = team;
    }

    public void setCompletedTask(TaskComponent completedTask) {
        completedTasks.add(completedTask);
        completedTask.setCompletedBy(this);
        score += completedTask.getValue();
    }

    public Player getPlayer() {return this.player;}
    public void setPlayer(Player player) {this.player = player;}
    public LockoutTeam getTeam() {return this.lockoutTeam;}
    public void setTeam(LockoutTeam lockoutTeam) {
        this.lockoutTeam = lockoutTeam;
    }
    public int getScore() {return this.score;}
    public int getTotalCompletedTasks() {
        return this.completedTasks.size();
    }
    public void setScoreboard(Scoreboard board) {
        this.board = board;
    }
    public void updateScoreboard() {
        player.setScoreboard(this.board);
    }
}
