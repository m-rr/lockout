package stretch.lockout.team;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import stretch.lockout.event.PlayerJoinTeamEvent;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.game.state.StateResettable;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.ui.inventory.TeamSelectionView;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TeamManager implements StateResettable {
    private Set<LockoutTeam> lockoutTeams;
    private Set<PlayerStat> playerStatCache;
    private Set<UUID> uuidCache;
    private boolean teamLock = false;
    private LockoutSettings settings;

    private TeamSelectionView teamSelectionView;

    public TeamManager(LockoutSettings settings) {
        this.lockoutTeams = new HashSet<>();
        this.playerStatCache = new HashSet<>();
        this.uuidCache = new HashSet<>();
        this.teamSelectionView = new TeamSelectionView();
        this.settings = settings;
    }

    public void updateSettings(LockoutSettings settings) {
        this.settings = settings;
    }

    public boolean isLocked() {
        return this.teamLock;
    }

    public void lock() {
        playerStatCache = getPlayerStats();
        uuidCache = getPlayerUUIDs();
        this.teamLock = true;
    }

    public void unlock() {
        playerStatCache = new HashSet<>();
        uuidCache = new HashSet<>();
        this.teamLock = false;
    }

    private boolean isValidPlayerStatCache() {
        return isLocked() && !playerStatCache.isEmpty();
    }

    private boolean isValidUUIDCache() {
        return isLocked() && !uuidCache.isEmpty();
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

    // True if teams are tied for first only.
    public boolean isTie() {
        int topScore = getWinningTeam().getScore();
        return getTeams().stream()
                .mapToInt(LockoutTeam::getScore)
                .filter(score -> score == topScore)
                .count() > 1;
    }

    public void removeTeamByName(String teamName) {
        getTeams().removeIf(team -> team.getName().equalsIgnoreCase(teamName));
    }

    public void removeTeam(LockoutTeam team) {
        getTeams().remove(team);
    }

    public Set<PlayerStat> getPlayerStats() {
        return isValidPlayerStatCache() ? playerStatCache : getTeams().stream()
                .flatMap(team -> team.getPlayerStats().stream())
                .collect(Collectors.toSet());
    }

    public PlayerStat getPlayerStat(Player player) {
        return getMappedPlayerStats().get(player);
    }

    public PlayerStat getPlayerStat(UUID uuid) {
        return getUUIDMappedPlayerStats().get(uuid);
    }

    public Set<UUID> getPlayerUUIDs() {
        return isValidUUIDCache() ? uuidCache : getPlayerStats().stream()
                .map(playerStat -> playerStat.getPlayer().getUniqueId())
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

    public boolean isPlayerOnTeam(final Player player) {
        //return getUUIDMappedPlayerStats().containsKey(player.getUniqueId());
        return getPlayerUUIDs().contains(player.getUniqueId());
    }

    public void doToAllPlayers(Consumer<Player> doAction) {
        getTeams().forEach(team -> team.doToPlayers(doAction));
    }

    public void addDefaultTeams() {
        for (int i = 0; i < DyeColor.values().length && i < settings.getDefaultTeams(); i++ ) {
            DyeColor dyeColor = DyeColor.values()[i];
            var team = new LockoutTeam(dyeColor.name(), settings.getTeamSize());
            team.setGuiItem(new ItemStack(Material.getMaterial(dyeColor.name() + "_WOOL")));
            addTeam(team);
        }
    }

    public boolean addTeam(LockoutTeam team) {
        if (getTeams().size() >= settings.getMaxTeams()) {
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
        if (isLocked()
                || getMappedTeams().containsKey(teamName)
                || getTeams().size() >= settings.getMaxTeams()) {
            return false;
        }

        LockoutTeam team = new LockoutTeam(teamName, settings.getTeamSize(), this);

        getTeams().add(team);
        teamSelectionView.addTeam(team);
        teamSelectionView.update();
        return true;
    }

    public boolean addPlayerToTeam(Player player, String teamName) {
        if (isLocked()
                || isPlayerOnTeam(player)
                || !isTeam(teamName)
                || getTeamByName(teamName).isFull()) {
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

    @Override
    public void reset() {
        destroyAllTeams();
        playerStatCache.clear();
        uuidCache.clear();
        unlock();
    }
}
