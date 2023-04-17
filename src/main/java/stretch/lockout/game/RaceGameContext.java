package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import stretch.lockout.Lockout;
import stretch.lockout.event.ReadyGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.listener.*;
import stretch.lockout.loot.LootManager;
import stretch.lockout.scoreboard.ScoreboardHandler;
import stretch.lockout.task.IndirectTaskListener;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.TaskManager;
import stretch.lockout.task.file.TaskList;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.util.MessageUtil;
import stretch.lockout.view.InventoryTaskView;
import stretch.lockout.view.TaskSelectionView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RaceGameContext {
    final private Lockout plugin;
    private TeamManager teamManager = new TeamManager();
    private TaskManager taskManager = new TaskManager();
    private final LootManager lootManager = new LootManager();
    private ScoreboardHandler scoreboardManager = new ScoreboardHandler();
    private PlayerTracker playerTracker = new PlayerTracker();
    private int maxScore;
    private GameState gameState;
    private final Set<HumanEntity> readyPlayers = new HashSet<>();
    private final Set<GameRule> rules = new HashSet<>();
    private int countdownTime = 10;

    public void setCountdownTime(int time) {
        this.countdownTime = time;
    }

    public RaceGameContext(final Lockout injectedPlugin) {
        this.plugin = injectedPlugin;
        this.maxScore = 0;
        new PlayerEventHandler(this);
        new EntityEventHandler(this);
        new BlockEventHandler(this);
        new TaskRaceEventHandler(this);
        new InventoryEventHandler(this);
        new IndirectTaskListener(this, 20);

        lootManager.setWorld(Bukkit.getWorld("world"));

        setGameState(GameState.UNINIT);

    }

    public Set<GameRule> gameRules() {
        return rules;
    }

    public ItemStack getGuiCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = compass.getItemMeta();

        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Tasks");
            compass.setItemMeta(itemMeta);
        }

        return compass;
    }

    public boolean hasGuiCompass(final HumanEntity player) {
        return player.getInventory().contains(getGuiCompass());
    }

    public ScoreboardHandler getScoreboardManager() {
        return scoreboardManager;
    }

    public TaskManager getTaskManager() {return taskManager;}

    public LootManager getLootManager() {return lootManager;}

    public TeamManager getTeamManager() {return teamManager;}

    public PlayerTracker getPlayerTracker() {return playerTracker;}

    public GameState getGameState() {return this.gameState;}

    public void playerReady(final HumanEntity player) {
        if (getGameState() != GameState.READY) {
            MessageUtil.log(player, ChatColor.RED + "Unable to set you to ready");
            return;
        }

        if (!taskManager.isTasksLoaded()) {
            MessageUtil.log(player, ChatColor.RED + "You must select tasks with the compass before ready up.");
            return;
        }

        if (readyPlayers.contains(player)) {
            MessageUtil.log(player, "You are already set as ready.");
        }
        else {
            readyPlayers.add(player);
            MessageUtil.log(player, "You have been set to ready.");
        }

        if (readyPlayers.size() >= Bukkit.getOnlinePlayers().size()) {
            setGameState(GameState.STARTING);
        }
    }

    public void playerUnready(final HumanEntity player) {

        if (readyPlayers.contains(player)) {
            readyPlayers.remove(player);
            MessageUtil.log(player, "You have been set to unready.");
        }
        else {
            MessageUtil.log(player, "You are already unready.");
        }
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        switch (gameState) {
            case PRE -> {
                if (gameRules().contains(GameRule.AUTO_LOAD)) {
                    TaskList taskList = new TaskList(getPlugin().getConfig().getString("autoLoadTask"));
                    if (getPlugin().getTaskLists().contains(taskList)) {
                        loadTaskList(getPlugin().getServer().getConsoleSender(), taskList);
                    }
                    else {
                        MessageUtil.consoleLog(ChatColor.RED + "TaskList " + taskList.taskName() + " was not found.");
                    }
                }
                setGameState(GameState.READY);
            }
            case READY -> {
                Bukkit.getPluginManager().callEvent(new ReadyGameEvent());
            }
            case STARTING -> {
                if (!getTaskManager().isTasksLoaded()) {
                    String message = ChatColor.RED + "You can not start without loading tasks!";
                    MessageUtil.consoleLog(message);
                    MessageUtil.sendAllChat(message);
                    setGameState(GameState.READY);
                    break;
                }

                readyPlayers.clear();

                // Make sure all players on are a team
                if (gameRules().contains(GameRule.FORCE_TEAM)) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (!teamManager.isPlayerOnTeam(player)) {
                            teamManager.createTeam(player.getName());
                            teamManager.addPlayerToTeam(player, player.getName());
                        }

                    });
                }

                if (gameRules().contains(GameRule.CLEAR_INV_START)) {
                    getTeamManager().clearAllPlayerEffectAndItems();
                }

                // Give all players compass
                getTeamManager().doToAllPlayers(player -> {
                    Inventory inventory = player.getInventory();
                    ItemStack compass = getGuiCompass();
                    if (!inventory.contains(compass)) {
                        inventory.addItem(compass);
                    }
                });

                playerTracker.setPlayers(getTeamManager().getPlayerStats());

                Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), playerTracker, 0, 5);

                MessageUtil.sendAllChat("Game Starting!");

                Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                        new CountDown(this, countdownTime,
                                getTeamManager().getPlayerStats().stream().map(PlayerStat::getPlayer).collect(Collectors.toSet())), 20);
                Bukkit.getPluginManager().callEvent(new StartGameEvent());
            }
            case RUNNING -> {
                Bukkit.getOnlinePlayers().forEach(player -> player.setInvulnerable(false));
            }
            case END -> {
                if (gameRules().contains(GameRule.CLEAN_INV_END)) {
                    getTeamManager().clearAllPlayerEffectAndItems();
                }

                MessageUtil.sendAllChat("Game ending.");

                Bukkit.getScheduler().cancelTasks(getPlugin());
                getScoreboardManager().resetScoreboard();
                scoreboardManager = new ScoreboardHandler();
                teamManager = new TeamManager();
                taskManager = new TaskManager();
                playerTracker = new PlayerTracker();
                setGameState(GameState.PRE);
            }
        }
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int score) {
        maxScore = score;
    }

    public long getRewardPotionTicks() {
        long rewardPotionTicks = 144000;
        return rewardPotionTicks;}

    public InventoryTaskView getInventoryTaskView() {
        var inventoryTaskView = new InventoryTaskView(gameRules().contains(GameRule.ALLOW_REWARD));
        HashSet<TaskComponent> guiTaskComponents = new HashSet<>(taskManager.getTasks());
        guiTaskComponents.forEach(inventoryTaskView::addTaskEntry);

        return inventoryTaskView;
    }

    public TaskSelectionView getTaskSelectionView() {
        var taskSelectionView = new TaskSelectionView();
        List<TaskList> guiTaskLists = getPlugin().getTaskLists();
        guiTaskLists.forEach(taskSelectionView::addTaskListEntry);

        return taskSelectionView;
    }

    public void loadTaskList(CommandSender sender, TaskList taskList) {
        getPlugin().loadScript(sender, taskList);
    }

  // Events must be player completable
    public void checkTask(Player player, Event event) {
        if (getGameState() == GameState.PAUSED) {
            return;
        }

        var currentTasks = taskManager.getMappedTasks();

        if (gameState == GameState.RUNNING && player.getGameMode() != GameMode.SPECTATOR && currentTasks.containsKey(event.getClass()) && teamManager.isPlayerOnTeam(player)) {
            var potentialTasks = currentTasks.get(event.getClass());
            for (var task : potentialTasks) {
                if (!task.isCompleted() && task.doesAccomplish(player, event)) {
                    PlayerStat playerStat = teamManager.getMappedPlayerStats().get(player);
                    taskManager.setTaskCompleted(playerStat, task);

                    TaskCompletedEvent taskEvent = new TaskCompletedEvent(task);
                    Bukkit.getPluginManager().callEvent(taskEvent);
                }
            }
        }
    }

    public void gracePeriod(HumanEntity player) {
        player.setInvulnerable(true);
        int GRACE_INVULNERABLE_TIME = 140;
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                () -> player.setInvulnerable(false), GRACE_INVULNERABLE_TIME);
    }

    public Lockout getPlugin() {
        return this.plugin;
    }
}
