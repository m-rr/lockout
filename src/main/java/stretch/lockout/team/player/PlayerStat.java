package stretch.lockout.team.player;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.team.LockoutTeam;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerStat {
    private Player player;
    final private Set<TaskComponent> completedTasks = new HashSet<>();

    private LockoutTeam lockoutTeam;
    private Scoreboard board;

    public PlayerStat(Player player, LockoutTeam team) {
        this.player = player;
        this.lockoutTeam = team;
    }

    public void setCompletedTask(TaskComponent completedTask) {
        completedTasks.add(completedTask);
        completedTask.setCompletedBy(this);
    }

    public int getSecondaryTasksCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void enqueueState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Player getPlayer() {return this.player;}
    public void setPlayer(Player player) {this.player = player;}
    public LockoutTeam getTeam() {return this.lockoutTeam;}
    public void setTeam(LockoutTeam lockoutTeam) {
        this.lockoutTeam = lockoutTeam;
    }
    public int getScore() {
        return completedTasks.stream().mapToInt(TaskComponent::getValue).sum();
    }
    public int getTotalCompletedTasks() {
        return this.completedTasks.size();
    }
    public void setScoreboard(Scoreboard board) {
        this.board = board;
    }
    public void updateScoreboard() {
        player.setScoreboard(this.board);
    }

    /** Uses team name and player uuid */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PlayerStat that = (PlayerStat) o;

        UUID thisUuid = player.getUniqueId();
        UUID thatUuid = that.getPlayer().getUniqueId();
        String thisTeamName = lockoutTeam.getName();
        String thatTeamName = that.getTeam().getName();

        return Objects.equals(thisUuid, thatUuid) &&
                Objects.equals(thisTeamName, thatTeamName);
    }

    /** Uses team name and player uuid */
    @Override
    public int hashCode() {
        UUID uuid = player.getUniqueId();
        String teamName = getTeam().getName();

        return Objects.hash(uuid, teamName);
    }
}
