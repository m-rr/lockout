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
import stretch.lockout.util.LockoutLogger;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class Lockout extends JavaPlugin {
    private final int pluginId = 19299;
    private LockoutContext lockout;
    private static Lockout instance;
    private static boolean devModeEnabled = false;
    private final String DEFAULT_TASK_NAME = "default/main.lua";

    public Lockout() {
        super();
    }

    public Lockout(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    public static Lockout getInstance() {return instance;}
    public static boolean isDevModeEnabled() {return devModeEnabled;}
    public void updateDevModeFlag(LockoutSettings settings) {
        devModeEnabled = settings.hasRule(LockoutGameRule.DEV);
    }

    @Override
    public void onEnable() {
        LockoutLogger.consoleLog("Enabling Lockout v" + getDescription().getVersion());
        instance = this;
        // Plugin startup logic
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                LockoutLogger.consoleLog(ChatColor.RED + "Failed to create plugin directory.");
            }
        }

        File defaultTasks = new File(getDataFolder(), DEFAULT_TASK_NAME);
        if (!defaultTasks.exists()) {
            saveResource(DEFAULT_TASK_NAME, false);
            getLogger().info("Created default task list.");
        }

        saveDefaultConfig();
        LockoutSettings gameSettings = generateConfig(false);
        updateDevModeFlag(gameSettings);
        LockoutLogger.debugLog(ChatColor.RED + "Lockout initialized in debug mode");

        BoardManager boardManager = new FileBasedBoardManager(this, gameSettings);

        this.lockout = new LockoutContext(this, gameSettings, boardManager);
        LockoutLogger.debugLog("Created lockoutContext object");

        lockout.getBoardManager().registerBoardsAsync();

        getCommand("lockout").setExecutor(new LockoutCommand(lockout));

        Metrics metrics = new Metrics(this, pluginId);

        lockout.getGameStateHandler().setGameState(GameState.PRE);
        LockoutLogger.consoleLog(ChatColor.GREEN + "Lockout v" + getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable() {
        if (lockout != null) {
            lockout.getUiManager().reset();
        }

    }

    public LockoutSettings generateConfig(boolean regen) {
        if (regen) {
            LockoutLogger.debugLog("Reloading configuration from disk");
            reloadConfig();
        }
        return new LockoutSettings(getConfig());
    }


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
