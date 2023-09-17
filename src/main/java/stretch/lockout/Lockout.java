package stretch.lockout;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import stretch.lockout.metrics.Metrics;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.LockoutCommand;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.util.MessageUtil;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class Lockout extends JavaPlugin {
    private final int pluginId = 19299;
    private RaceGameContext taskRaceContext;
    private final String DEFAULT_TASK_NAME = "default.tasks";
    private FileConfiguration config;

    public Lockout() {
        super();
    }

    public Lockout(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                MessageUtil.consoleLog(ChatColor.RED + "Failed to create plugin directory.");
            }
        }

        File defaultTasks = new File(getDataFolder(), DEFAULT_TASK_NAME);
        if (!defaultTasks.exists()) {
            saveResource(DEFAULT_TASK_NAME, false);
            MessageUtil.consoleLog("Create default task list.");
        }

        taskRaceContext = new RaceGameContext(this);

        saveDefaultConfig();
        setConfig(taskRaceContext);

        getCommand("lockout")
                .setExecutor(new LockoutCommand(taskRaceContext, config));

        Metrics metrics = new Metrics(this, pluginId);

        taskRaceContext.getGameStateHandler().setGameState(GameState.PRE);
    }

    @Override
    public void onDisable() {
        if (taskRaceContext != null) {
            taskRaceContext.getScoreboardManager().resetScoreboard();
            taskRaceContext.destroyBars();
        }
    }

    private void setConfig(RaceGameContext taskRaceContext) {
        config = getConfig();
        if (config.getBoolean("clearInventoryStart")) {
            taskRaceContext.gameRules().add(GameRule.CLEAR_INV_START);
        }
        if (config.getBoolean("clearInventoryEnd")) {
            taskRaceContext.gameRules().add(GameRule.CLEAN_INV_END);
        }
        if (config.getBoolean("moveDuringCountdown")) {
            taskRaceContext.gameRules().add(GameRule.COUNTDOWN_MOVE);
        }
        taskRaceContext.setCountdownTime(config.getInt("countdownDuration"));
        if (config.getBoolean("forcePlayersOnTeam")) {
            taskRaceContext.gameRules().add(GameRule.FORCE_TEAM);
        }
        if (config.getBoolean("allowCompassTracking")) {
            taskRaceContext.gameRules().add(GameRule.COMPASS_TRACKING);
        }
        if (config.getBoolean("useMaxScore")) {
            taskRaceContext.gameRules().add(GameRule.MAX_SCORE);
        }
        if (config.getBoolean("commandsRequireOp")) {
            taskRaceContext.gameRules().add(GameRule.OP_COMMANDS);
        }
        if (config.getBoolean("useTimer")) {
            taskRaceContext.gameRules().add(GameRule.TIMER);
        }
        if (config.getBoolean("useTieBreaker")) {
            taskRaceContext.gameRules().add(GameRule.TIE_BREAK);
        }

        // Loot
        if (config.getBoolean("lootSpawn")) {
            taskRaceContext.gameRules().add(GameRule.SPAWN_LOOT);
            taskRaceContext.getLootManager().setLootSpawnChance(config.getInt("lootChance"));
            if (config.getBoolean("lootSpawnNearPlayers")) {
                taskRaceContext.gameRules().add(GameRule.LOOT_NEAR);
                taskRaceContext.getLootManager().setSpawnRadius(config.getInt("lootSpawnRadius"));
            }
        }

        // Tasks
        if (config.getBoolean("autoLoad")) {
            taskRaceContext.gameRules().add(GameRule.AUTO_LOAD);
        }

        // Reward
        if (config.getBoolean("enableReward")) {
            taskRaceContext.gameRules().add(GameRule.ALLOW_REWARD);
        }

        // World
        String worldName = Optional.ofNullable(config.getString("world"))
                .orElse("world");
        taskRaceContext.setGameWorld(worldName);
        Long startTime = Optional.of(config.getLong("startTime"))
                .orElse(1000L);
        taskRaceContext.setStartTime(startTime);

        // Teams
        int teamCount = Optional.of(config.getInt("defaultTeams"))
                .orElse(0);
        taskRaceContext.getTeamManager().setDefaultTeams(teamCount);
        int teamSize = Optional.of(config.getInt("teamSize"))
                .orElse(16);
        taskRaceContext.getTeamManager().setTeamSize(teamSize);
        int maxTeams = Optional.of(config.getInt("maxTeams"))
                .orElse(8);
        taskRaceContext.getTeamManager().setMaxTeams(maxTeams);
    }

    public String getTaskFileExtension() {return ".tasks";}
    public String getDataFolderRelativePath() {return "plugins/Lockout/";}

    // Should instead return config file which lists boards
    public List<String> getTaskLists() {
        return Stream.of(new File(getDataFolder().getAbsolutePath()).listFiles())
                .filter(file -> file.getName().contains(".tasks"))
                .map(file ->
                        file.getName()
                                .substring(0, file.getName().lastIndexOf(".")))
                .toList();
    }

    public RaceGameContext getTaskRaceContext() {
        return taskRaceContext;
    }

}
