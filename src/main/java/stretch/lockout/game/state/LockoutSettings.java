package stretch.lockout.game.state;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.util.LockoutLogger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LockoutSettings {
    static public final int DEFAULT_COUNTDOWN_TIME = 10;
    static public final int DEFAULT_RESPAWN_KIT_TIME = 300;
    static public final int DEFAULT_RESPAWN_KIT_COOLDOWN = 180;
    static public final int DEFAULT_RESPAWN_INVULNERABLE = 140;
    static public final long DEFAULT_START_TIME = 1000L;
    static public final long DEFAULT_REWARD_POTION_TICKS = 144000L;
    static public final long DEFAULT_PLAYER_STATE_TICKS = 1L;
    static public final int DEFAULT_BASIC_TEAMS = 0;
    static public final int DEFAULT_TEAM_SIZE = 16;
    static public final int DEFAULT_MAX_TEAMS = 8;
    static public final int DEFAULT_MAX_SCORE = 10;
    static public final int DEFAULT_TIMER_SECONDS = 610 * 24 * 60;

    private final Set<LockoutGameRule> rules = new HashSet<>();
    private int countdownTime;
    private int respawnKitTime;
    private int respawnInvulnerabilityTime;
    private int respawnCooldownTime;
    private int defaultTeams;
    private int teamSize;
    private int maxTeams;
    private int maxScore;
    private int timerTime;
    private long playerStateUpdateTicks;
    private long startTime;
    private World gameWorld;

    public LockoutSettings(ConfigurationSection config) {
        if (config.getBoolean("clearInventoryStart")) {
            rules.add(LockoutGameRule.CLEAR_INV_START);
        }
        if (config.getBoolean("clearInventoryEnd")) {
            rules.add(LockoutGameRule.CLEAN_INV_END);
        }
        if (config.getBoolean("startAtSpawn")) {
            rules.add(LockoutGameRule.START_SPAWN);
        }
        if (config.getBoolean("checkUpdate")) {
            rules.add(LockoutGameRule.CHECK_UPDATE);
        }
        if (config.getBoolean("moveDuringCountdown")) {
            rules.add(LockoutGameRule.COUNTDOWN_MOVE);
        }
        if (config.getBoolean("revokeAdvancements")) {
            rules.add(LockoutGameRule.REVOKE_ADVANCEMENT);
        }

        this.countdownTime = Optional
                .of(config.getInt("countdownDuration"))
                .orElse(DEFAULT_COUNTDOWN_TIME);

        if (config.getBoolean("forcePlayersOnTeam")) {
            rules.add(LockoutGameRule.FORCE_TEAM);
        }
        if (config.getBoolean("allowCompassTracking")) {
            rules.add(LockoutGameRule.COMPASS_TRACKING);
        }
        if (config.getBoolean("useMaxScore")) {
            rules.add(LockoutGameRule.MAX_SCORE);
        }
        if (config.getBoolean("commandsRequireOp")) {
            rules.add(LockoutGameRule.OP_COMMANDS);
        }
        if (config.getBoolean("useTimer")) {
            rules.add(LockoutGameRule.TIMER);
        }
        if (config.getBoolean("useTieBreaker")) {
            rules.add(LockoutGameRule.TIE_BREAK);
        }
        if (config.getBoolean("dev")) {
            rules.add(LockoutGameRule.DEV);
        }
        if (config.getBoolean("useRespawnKit")) {
            rules.add(LockoutGameRule.RESPAWN_KIT);
        }
        if (config.getBoolean("respawnInvulnerability")) {
            rules.add(LockoutGameRule.RESPAWN_INVULNERABLE);
        }

        this.respawnKitTime = Optional
                .of(config.getInt("respawnKitTime"))
                .orElse(DEFAULT_RESPAWN_KIT_TIME);

        this.respawnCooldownTime = Optional
                .of(config.getInt("RespawnKitCooldown"))
                .orElse(DEFAULT_RESPAWN_KIT_COOLDOWN);

        this.respawnInvulnerabilityTime = Optional
                .of(config.getInt("respawnInvulnerabilityTime"))
                .orElse(DEFAULT_RESPAWN_INVULNERABLE);

        if (config.getBoolean("autoLoad")) {
            rules.add(LockoutGameRule.AUTO_LOAD);
        }

        if (config.getBoolean("enableReward")) {
            rules.add(LockoutGameRule.ALLOW_REWARD);
        }

        String worldName = Optional
                .ofNullable(config.getString("world"))
                .orElse("world");
        this.gameWorld = Bukkit.getWorld(worldName);
        this.startTime = Optional.of(config.getLong("startTime"))
                .orElse(DEFAULT_START_TIME);
        this.playerStateUpdateTicks = Optional.of(config.getLong("playerStateUpdateTicks"))
            .orElse(DEFAULT_PLAYER_STATE_TICKS);
        this.timerTime = Optional.of(config.getInt("setTimer"))
            .orElse(DEFAULT_TIMER_SECONDS);
        this.defaultTeams = Optional.of(config.getInt("defaultTeams"))
                .orElse(DEFAULT_BASIC_TEAMS);
        this.teamSize = Optional.of(config.getInt("teamSize"))
                .orElse(DEFAULT_TEAM_SIZE);
        this.maxTeams = Optional.of(config.getInt("maxTeams"))
                .orElse(DEFAULT_MAX_TEAMS);

    }

public void showDiff(final LockoutSettings other) {
        Set<LockoutGameRule> newRule = new HashSet<>(rules);
        newRule.removeAll(other.gameRules());
        other.gameRules().removeAll(rules);
        for (var r : newRule) {
            LockoutLogger.debugLog(r.name() + ": False => True");
        }
        for (var r : other.gameRules()) {
            LockoutLogger.debugLog(r.name() + ": True => False");

        }

        if (gameWorld != other.getGameWorld()) {
            LockoutLogger.debugLog("WORLD: "
                    + other.getGameWorld().getName() + " => " + gameWorld.getName());
        }

        displaySettingDiff("COUNTDOWN_DURATION", countdownTime, other.getCountdownTime());
        displaySettingDiff("RESPAWN_KIT_TIME", respawnKitTime, other.getRespawnKitTime());
        displaySettingDiff("RESPAWN_INVULNERABILITY_TIME", respawnInvulnerabilityTime, other.getRespawnInvulnerabilityTime());
        displaySettingDiff("RESPAWN_KIT_COOLDOWN", respawnCooldownTime, other.getRespawnCooldownTime());
        displaySettingDiff("DEFAULT_TEAMS", defaultTeams, other.getDefaultTeams());
        displaySettingDiff("TEAM_SIZE", teamSize, other.getTeamSize());
        displaySettingDiff("MAX_TEAMS", maxTeams, other.getMaxTeams());
        displaySettingDiff("MAX_SCORE", maxScore, other.getMaxScore());
        displaySettingDiff("START_TIME", (int) startTime, (int) other.getStartTime());
        displaySettingDiff("TIMER_TIME", timerTime, other.getTimerSeconds());
    }

    private void displaySettingDiff(String name, int newVal, int oldVal) {
        if (newVal != oldVal) {
            LockoutLogger.debugLog(name + ": " + oldVal + " => " + newVal);
            //MessageUtil.debugLog(this, StringTemplate.STR."\{name}: \{oldVal} => \{newVal}");
        }
    }

    public boolean hasRule(final LockoutGameRule rule) {
        return gameRules().contains(rule);
    }

    public Set<LockoutGameRule> gameRules() {return rules;}
    public int getCountdownTime() {return countdownTime;}
    public int getTeamSize() {return teamSize;}
    public int getMaxTeams() {return maxTeams;}
    public int getDefaultTeams() {return defaultTeams;}
    public int getRespawnKitTime() {return respawnKitTime;}
    public int getRespawnCooldownTime() {return respawnCooldownTime;}
    public int getRespawnInvulnerabilityTime() {return respawnInvulnerabilityTime;}
    public int getMaxScore() {return maxScore;}
    public int getTimerSeconds() {return timerTime;}
    public long getStartTime() {return startTime;}
    public long getRewardPotionTicks() {return DEFAULT_REWARD_POTION_TICKS;}
    public long getPlayerUpdateTicks() {return playerStateUpdateTicks;}
    public World getGameWorld() {return gameWorld;}

    public void setMaxScore(final int maxScore) {
        this.maxScore = maxScore;
    }

    public void setCountdownTime(final int countdownTime) {
        this.countdownTime = countdownTime;
    }

    public void setRespawnKitTime(final int respawnKitTime) {
        this.respawnKitTime = respawnKitTime;
    }

    public void setRespawnCooldownTime(final int respawnCooldownTime) {this.respawnCooldownTime = respawnCooldownTime;}

    public void setRespawnInvulnerabilityTime(final int respawnInvulnerabilityTime) {
        this.respawnInvulnerabilityTime = respawnInvulnerabilityTime;
    }

    public void setDefaultTeams(final int defaultTeams) {
        this.defaultTeams = defaultTeams;
    }

    public void setTeamSize(final int teamSize) {
        this.teamSize = teamSize;
    }

    public void setMaxTeams(final int maxTeams) {
        this.maxTeams = maxTeams;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public void setTimerSeconds(final int seconds) {
        this.timerTime = seconds;
    }

    public void setPlayerUpdateTicks(final long ticks) {
        this.playerStateUpdateTicks = ticks;
    }

    public void setGameWorld(final World gameWorld) {
        this.gameWorld = gameWorld;
    }
}
