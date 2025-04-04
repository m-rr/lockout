package stretch.lockout;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import stretch.lockout.board.BoardManager;
import stretch.lockout.board.FileBasedBoardManager;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.platform.Metrics;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.LockoutCommand;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.util.MessageUtil;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class Lockout extends JavaPlugin {
    private final int pluginId = 19299;
    private LockoutContext lockout;
    private final String DEFAULT_TASK_NAME = "default/main.lua";

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
            MessageUtil.consoleLog("Created default task list.");
        }

        saveDefaultConfig();
        LockoutSettings gameSettings = generateConfig(false);
        MessageUtil.debugLog(gameSettings, ChatColor.RED + "Lockout initialized in debug mode");

        BoardManager boardManager = new FileBasedBoardManager(this, gameSettings);

        lockout = new LockoutContext(this, gameSettings, boardManager);
        MessageUtil.debugLog(gameSettings, "Created lockoutContext object");

        lockout.getBoardManager().registerBoardsAsync();

        if (gameSettings.hasRule(LockoutGameRule.DEV)) {
            lockout.getUserLuaEnvironment().initUserChunk();
        }

        getCommand("lockout")
                .setExecutor(new LockoutCommand(lockout));

        Metrics metrics = new Metrics(this, pluginId);

        lockout.getGameStateHandler().setGameState(GameState.PRE);
    }

    @Override
    public void onDisable() {
        if (lockout != null) {
            lockout.getUiManager().reset();
        }

    }

    public LockoutSettings generateConfig(boolean regen) {
        if (regen) {
            MessageUtil.debugLog(lockout.settings(), "Updating configuration from disk");
            reloadConfig();
        }
        return new LockoutSettings(getConfig());
    }



    public String getTaskFileExtension() {return ".tasks";}
    public String getDataFolderRelativePath() {return "plugins/Lockout/";}

    // Should instead return config file which lists boards
    public List<String> getTaskLists() {
        return Stream.of(new File(getDataFolder().getAbsolutePath()).listFiles())
                .filter(file -> file.getName().contains(".lua"))
                .map(file ->
                        file.getName()
                                .substring(0, file.getName().lastIndexOf(".")))
                .toList();
    }

    public LockoutContext getLockout() {
        return lockout;
    }

}
