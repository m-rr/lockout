package stretch.lockout.team;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.function.Consumer;

public class LockoutTeam {
    final protected HashSet<PlayerStat> players = new HashSet<>();
    final protected String teamName;
    protected Scoreboard board;
    protected TeamManager teamManager;

    public LockoutTeam(@Nullable TeamManager teamManager, String teamName) {
        this.teamName = teamName;
        this.teamManager = teamManager;
    }

    public void setTeamManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }
    public TeamManager getTeamManager() {return teamManager;}

    public void doToOpposingTeams(Consumer<Player> action) {
        getTeamManager().getOpposingTeams(this)
                .forEach(team -> team.doToPlayers(action));
    }

    public String getName() {return this.teamName;}

    public boolean containsPlayer(Player player) {
        var UUID = player.getUniqueId();
        return players.stream()
                .anyMatch(playerStat -> playerStat.getPlayer().getUniqueId() == UUID);
    }

    public boolean containsPlayer(PlayerStat playerStat) {
        return players.contains(playerStat);
    }

    public HashSet<PlayerStat> getPlayerStats() {return this.players;}

    public void setScoreboard(Scoreboard board) {
        this.board = board;
    }

    public void updateTeamScoreboard() {
        players.forEach(playerStat -> {
            playerStat.setScoreboard(board);
            playerStat.updateScoreboard();
        });
    }

    public void addPlayer(PlayerStat playerStat) {
        players.add(playerStat);
        //playerStat.setScoreboard(board);
        //playerStat.updateScoreboard();
    }

    public void removePlayer(Player player) {
        var playerUUID = player.getUniqueId();
        players.removeIf(playerStat -> playerStat.getPlayer().getUniqueId() == playerUUID);
    }

    public void removePlayer(PlayerStat playerStat) {
        players.remove(playerStat);
    }

    // Team score is sum of players' scores -> if player leaves team it messes up score
    public int getScore() {
        return players.stream()
                .mapToInt(PlayerStat::getScore)
                .reduce(0, Integer::sum);
    }

    public int playerCount() {
        return this.players.size();
    }

    public void doToPlayers(Consumer<Player> action) {
        players.forEach(playerStat -> action.accept(playerStat.getPlayer()));
    }

    public void sendMessage(String message) {
        doToPlayers(player -> player.sendRawMessage(message));
    }

}
