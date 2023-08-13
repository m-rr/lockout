package stretch.lockout.team;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import stretch.lockout.event.PlayerJoinTeamEvent;
import stretch.lockout.view.TeamSelectionView;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TeamManager {
    private Set<LockoutTeam> lockoutTeams;

    private TeamSelectionView teamSelectionView;
    private int defaultTeams = 0;
    private int teamSize = 16;
    private int maxTeams = 8;

    public TeamManager() {
        this.lockoutTeams = new HashSet<>();
        this.teamSelectionView = new TeamSelectionView();
    }

    public TeamManager(Collection<LockoutTeam> teams) {
        teams.forEach(team -> team.setTeamManager(this));
        this.lockoutTeams = new HashSet<>(teams);
        this.teamSelectionView = new TeamSelectionView();
    }

    public TeamSelectionView getTeamSelectionView() {
        return teamSelectionView;
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

    public void addDefaultTeams() {
        for (int i = 0; i < DyeColor.values().length && i < defaultTeams; i++ ) {
            DyeColor dyeColor = DyeColor.values()[i];
            var team = new LockoutTeam(dyeColor.name(), teamSize);
            team.setGuiItem(new ItemStack(Material.getMaterial(dyeColor.name() + "_WOOL")));
            addTeam(team);
        }
    }

    public void setDefaultTeams(final int defaultTeams) {this.defaultTeams = defaultTeams;}
    public void setTeamSize(final int teamSize) {this.teamSize = teamSize;}
    public void setMaxTeams(final int maxTeams) {this.maxTeams = maxTeams;}
    public int getMaxTeams() {return this.maxTeams;}
    public int getTeamSize() {return this.teamSize;}
    public int getDefaultTeams() {return this.defaultTeams;}

    public boolean addTeam(LockoutTeam team) {
        if (getTeams().size() >= maxTeams) {
            return false;
        }
        team.setTeamManager(this);
        lockoutTeams.add(team);
        teamSelectionView.addTeam(team);
        teamSelectionView.update();
        return true;
    }

    // Safe to try
    public boolean createTeam(String teamName) {
        if (getMappedTeams().containsKey(teamName) || getTeams().size() >= maxTeams) {
            return false;
        }

        LockoutTeam team = new LockoutTeam(teamName, teamSize, this);

        getTeams().add(team);
        teamSelectionView.addTeam(team);
        teamSelectionView.update();
        return true;
    }

    public boolean addPlayerToTeam(Player player, String teamName) {
        if (isPlayerOnTeam(player) || !isTeam(teamName) || getTeamByName(teamName).isFull()) {
            return false;
        }

        LockoutTeam team = getTeamByName(teamName);
        PlayerStat playerStat = new PlayerStat(player, team);
        team.addPlayer(playerStat);
        Bukkit.getPluginManager().callEvent(new PlayerJoinTeamEvent(playerStat));
        teamSelectionView.update();
        return true;
    }

    public void destroyAllTeams() {
        this.lockoutTeams = new HashSet<>();
        this.teamSelectionView = new TeamSelectionView();
    }
}
