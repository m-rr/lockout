package stretch.lockout;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.task.file.TaskList;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;
import stretch.lockout.util.WorldUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Lockout extends JavaPlugin {
    private RaceGameContext taskRaceContext;
    private IFn loadScript;
    private IFn loadString;
    private final String DEFAULTTASKNAME = "default.tasks";
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

        File defaultTasks = new File(getDataFolder(), DEFAULTTASKNAME);
        if (!defaultTasks.exists()) {
            saveResource(DEFAULTTASKNAME, false);
            MessageUtil.consoleLog("Create default task list.");
        }

        taskRaceContext = new RaceGameContext(this);

        saveDefaultConfig();
        setConfig(taskRaceContext);

        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        Clojure.var("clojure.core", "require").invoke(Clojure.read("stretch.lockout.api"));
        loadScript = Clojure.var("stretch.lockout.api", "load-script");
        loadString = Clojure.var("stretch.lockout.api", "load-data");

        taskRaceContext.setGameState(GameState.PRE);

    }

    @Override
    public void onDisable() {
        if (taskRaceContext != null) {
            taskRaceContext.getScoreboardManager().resetScoreboard();
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

        // LOOT
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
    }

    public void loadResourceScript(CommandSender sender, String resourceName) {
        try {
            String data = new String(getResource(resourceName).readAllBytes(), StandardCharsets.UTF_8);
            loadString(sender, data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadString(CommandSender sender, String data) {
        try {
            loadString.invoke(data);
            MessageUtil.log(sender, "Forms evaluated from string.");
        }
        catch (Throwable e) {
            e.printStackTrace();
            MessageUtil.log(sender, ChatColor.RED + "Forms could not be evaluated.");
        }
    }

    public void loadScript(CommandSender sender, TaskList taskList) {
        try {
            loadScript.invoke(Clojure.read(getDataFolderRelativePath() + taskList.taskName() + getTaskFileExtension()));
            MessageUtil.log(sender, "Tasks from " + taskList.taskName() + " loaded.");
        }
        catch (Throwable e) {
            MessageUtil.log(sender, ChatColor.RED + "Tasks from " + taskList.taskName() + " could not be loaded.");
            e.printStackTrace();
        }
    }

    public String getTaskFileExtension() {return ".tasks";}
    public String getDataFolderRelativePath() {return "plugins/Lockout/";}

    // Should instead return config file which lists boards
    public List<TaskList> getTaskLists() {
        return Stream.of(new File(getDataFolder().getAbsolutePath()).listFiles())
                .filter(file -> file.getName().contains(".tasks"))
                .map(file -> new TaskList(file.getName().substring(0, file.getName().lastIndexOf("."))))
                .toList();
    }

    public RaceGameContext getTaskRaceContext() {
        return taskRaceContext;
    }

    private void noPermissionMessage(CommandSender sender) {
        MessageUtil.log(sender, ChatColor.RED + "You do not have permission to use that.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player player && (cmd.getName().equalsIgnoreCase("ready") ||
                cmd.getName().equalsIgnoreCase("unready"))) {
            if (!player.hasPermission("lockout.ready") || !config.getBoolean("allowPlayerReady")) {
                noPermissionMessage(player);
                return true;
            }

            switch(cmd.getName().toLowerCase(Locale.ROOT)) {
                case "ready" -> {
                    taskRaceContext.playerReady(player);
                }
                case "unready" -> {
                    taskRaceContext.playerUnready(player);
                }
                default -> {return false;}
            }


            return true;
        }

        if (sender instanceof Player player && cmd.getName().equalsIgnoreCase("lockout") && args.length >= 1) {
            String command = args[0];
            switch (command) {
                case "team" -> {
                    if (!player.hasPermission("lockout.team")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (args.length == 2) {
                        String teamName = args[1];
                        TeamManager teamManager = taskRaceContext.getTeamManager();
                        if (teamManager.isPlayerOnTeam(player)) {
                            MessageUtil.sendChat(player, "You are already on a team!");
                            return true;
                        }
                        if (teamManager.createTeam(teamName)) {
                            MessageUtil.sendAllChat(player.getName() + " created team " + ChatColor.GOLD + teamName);
                        }
                        teamManager.addPlayerToTeam(player, teamName);
                        if (!taskRaceContext.hasGuiCompass(player)) {
                            player.getInventory().addItem(taskRaceContext.getGuiCompass());
                        }
                    }
                    else {
                        return false;
                    }
                }
                case "compass" -> {
                    if (!player.hasPermission("lockout.compass")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    Inventory playerInv = player.getInventory();
                    playerInv.addItem(taskRaceContext.getGuiCompass());
                }
                case "start" -> {
                    if (!player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (taskRaceContext.getGameState() != GameState.READY) {
                        MessageUtil.sendChat(player, "The game is already started!");
                    }
                    else {
                        taskRaceContext.setGameState(GameState.STARTING);
                    }

                }
                case "end" -> {
                    if (!player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    taskRaceContext.setGameState(GameState.END);
                }
                case "debug" -> {
                    final int EDGE = 20;
                    final long seed = player.getWorld().getSeed();
                    Predicate<ChunkSnapshot> pred = (c) -> {
                        int chunkX = c.getX();
                        int chunkY = c.getZ();

                        Random random = new Random();

                        byte villageParam1 = 32;
                        byte villageParam2 = 8;

                        int k = chunkX;
                        int m = chunkY;
                        if (chunkX < 0) chunkX -= villageParam1 - 1;
                        if (chunkY < 0) chunkY -= villageParam1 - 1;

                        int n = chunkX / villageParam1;
                        int i1 = chunkY / villageParam1;

                        long positionSeed = n * 341873128712L + i1 * 132897987541L + seed + 10387312L;
                        random.setSeed(positionSeed);

                        n *= villageParam1;
                        i1 *= villageParam1;
                        n += random.nextInt(villageParam1 - villageParam2);
                        i1 += random.nextInt(villageParam1 - villageParam2);
                        chunkX = k;
                        chunkY = m;
                        return (chunkX == n) && (chunkY == i1);
                            //return MinecraftUtil.isValidBiome(chunkX * 16 + 8, chunkY * 16 + 8, 0, validBiomes);
                    };
                    Predicate<Chunk> predBell = (c) -> WorldUtil.materialsInChunk(c).contains(Material.BELL);
                    //player.sendMessage("Has Bell: " + predBell.test(player.getLocation().getChunk()));
                    //player.sendMessage("Has village: " + pred.test(player.getLocation().getChunk().getChunkSnapshot()));
                    player.sendMessage("result: " + WorldUtil.findValidChunk(player.getLocation(), EDGE, pred, player));
                }
                default -> {return false;}
            }
            return true;
        }

        return false;
    }

}
