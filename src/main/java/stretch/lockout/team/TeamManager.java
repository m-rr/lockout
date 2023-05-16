package stretch.lockout.team;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import stretch.lockout.event.PlayerJoinTeamEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TeamManager {
    private final Set<LockoutTeam> lockoutTeams;

    public TeamManager() {
        this.lockoutTeams = new HashSet<>();
    }

    public TeamManager(Collection<LockoutTeam> teams) {
        teams.forEach(team -> team.setTeamManager(this));
        this.lockoutTeams = new HashSet<>(teams);
    }

    public List<String> getTeamNames() {
        return getTeams().stream()
                .map(LockoutTeam::getName)
                .sorted()
                .toList();
    }

    public void clearAllPlayerEffectAndItems() {
        doToAllPlayers(player -> {
            player.getInventory().clear();
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
        });
    }

    public int teamCount() {return getTeams().size();}

    public Set<LockoutTeam> getTeams() {
        return lockoutTeams;
    }

    public Map<String, LockoutTeam> getMappedTeams() {
        return getTeams().stream()
                .collect(Collectors.toMap(
                        LockoutTeam::getName,
                        team -> team
                ));
    }

    public boolean isTeam(String teamName) {
        return getMappedTeams().containsKey(teamName);
    }

    public LockoutTeam getTeamByName(String teamName) {
        return getTeams().stream()
                .filter(team -> team.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public Set<LockoutTeam> getOpposingTeams(LockoutTeam team) {
        return getTeams().stream()
                .filter(lockoutTeam -> lockoutTeam != team)
                .collect(Collectors.toSet());
    }

    public LockoutTeam getWinningTeam() {
        return getTeams().stream()
                .max(Comparator.comparing(LockoutTeam::getScore))
                .orElseThrow(NoSuchElementException::new);
    }

    public void removeTeamByName(String teamName) {
        getTeams().removeIf(team -> team.getName().equalsIgnoreCase(teamName));
    }

    public void removeTeam(LockoutTeam team) {
        getTeams().remove(team);
    }

    public Set<PlayerStat> getPlayerStats() {
        return getTeams().stream()
                .flatMap(team -> team.getPlayerStats().stream())
                .collect(Collectors.toSet());
    }

    public Map<UUID, PlayerStat> getUUIDMappedPlayerStats() {
        return getPlayerStats().stream()
                .collect(Collectors.toMap(
                        playerStat -> playerStat.getPlayer().getUniqueId(),
                        playerStat -> playerStat
                ));
    }

    public Map<Player, PlayerStat> getMappedPlayerStats() {
        return getTeams().stream()
                .flatMap(team -> team.getPlayerStats().stream())
                .collect(Collectors.toMap(
                        PlayerStat::getPlayer,
                        playerStat -> playerStat
                ));
    }

    public boolean isPlayerOnTeam(Player player) {
        return getUUIDMappedPlayerStats().containsKey(player.getUniqueId());
    }

    public void doToAllPlayers(Consumer<Player> doAction) {
        getTeams().forEach(team -> team.doToPlayers(doAction));
    }

    // Safe to try
    public boolean createTeam(String teamName) {
        if (getMappedTeams().containsKey(teamName)) {
            return false;
        }

        getTeams().add(new LockoutTeam(this, teamName));
        return true;
    }

    public boolean addPlayerToTeam(Player player, String teamName) {
        if (isPlayerOnTeam(player) || !isTeam(teamName)) {
            return false;
        }

        LockoutTeam team = getTeamByName(teamName);
        PlayerStat playerStat = new PlayerStat(player, team);
        team.addPlayer(playerStat);
        Bukkit.getPluginManager().callEvent(new PlayerJoinTeamEvent(playerStat));
        return true;
    }

}
