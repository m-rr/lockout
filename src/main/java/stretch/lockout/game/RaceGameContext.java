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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import stretch.lockout.Lockout;
import stretch.lockout.event.CountDown;
import stretch.lockout.event.ReadyGameEvent;
import stretch.lockout.event.StartGameEvent;
import stretch.lockout.event.TaskCompletedEvent;
import stretch.lockout.listener.*;
import stretch.lockout.loot.LootManager;
import stretch.lockout.scoreboard.ScoreboardHandler;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.TaskManager;
import stretch.lockout.task.file.TaskList;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.view.InventoryTaskView;
import stretch.lockout.view.TaskSelectionView;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class RaceGameContext {
    final private Lockout plugin;
    private TeamManager teamManager = new TeamManager();
    private TaskManager taskManager = new TaskManager();
    private LootManager lootManager = new LootManager();
    private ScoreboardHandler scoreboardManager = new ScoreboardHandler();
    private Scoreboard board;
    private Objective boardObjective;
    private int maxScore;
    private final long rewardPotionTicks = 144000;
    private final int GRACE_INVULNERABLE_TIME = 140;
    private GameState gameState;
    private final Random random = new Random();
    private final Set<HumanEntity> readyPlayers = new HashSet<>();
    private Set<GameRule> rules = new HashSet<>();
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

    public GameState getGameState() {return this.gameState;}

    public void playerReady(final HumanEntity player) {
        if (getGameState() != GameState.READY) {
            getPlugin().consoleLogMessage(player, ChatColor.RED + "Unable to set you to ready.");
            return;
        }

        if (!taskManager.isTasksLoaded()) {
            getPlugin().consoleLogMessage(player, ChatColor.RED + "You must select tasks with the compass before ready up.");
            return;
        }

        if (readyPlayers.contains(player)) {
            getPlugin().consoleLogMessage(player, "You are already set as ready.");
        }
        else {
            readyPlayers.add(player);
            getPlugin().consoleLogMessage(player, "You have been set to ready.");
        }

        if (readyPlayers.size() >= Bukkit.getOnlinePlayers().size()) {
            setGameState(GameState.STARTING);
        }
    }

    public void playerUnready(final HumanEntity player) {

        if (readyPlayers.contains(player)) {
            readyPlayers.remove(player);
            getPlugin().consoleLogMessage(player, "You have been set to unready.");
        }
        else {
            getPlugin().consoleLogMessage(player, "You are already unready.");
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
                        getPlugin().consoleLogMessage(ChatColor.RED + "TaskList " + taskList.taskName() + " was not found.");
                    }
                }
                setGameState(GameState.READY);
            }
            case READY -> {
                Bukkit.getPluginManager().callEvent(new ReadyGameEvent());
            }
            case STARTING -> {
                if (!getTaskManager().isTasksLoaded()) {
                    String message = "You can not start without loading tasks!";
                    getPlugin().consoleLogMessage(message);
                    getTeamManager().doToAllPlayers(player -> getPlugin().consoleLogMessage(player, message));
                    setGameState(GameState.READY);
                    break;
                }

                readyPlayers.clear();

                // Make sure all players on are a team
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (!teamManager.isPlayerOnTeam(player)) {
                        teamManager.createTeam(player.getName());
                        teamManager.addPlayerToTeam(player, player.getName());
                    }
                    if (gameRules().contains(GameRule.CLEAR_INV_START)) {
                        getTeamManager().clearAllPlayerEffectAndItems();
                    }
                    player.getInventory().addItem(getGuiCompass());
                });

                getTeamManager().doToAllPlayers(player -> getPlugin().consoleLogMessage(player, "Game starting!"));

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
                getTeamManager().doToAllPlayers(player -> getPlugin().consoleLogMessage(player, "Game ending."));

                Bukkit.getScheduler().cancelTasks(getPlugin());
                getScoreboardManager().resetScoreboard();
                scoreboardManager = new ScoreboardHandler();
                teamManager = new TeamManager();
                taskManager = new TaskManager();
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

    public long getRewardPotionTicks() {return rewardPotionTicks;}

    public InventoryTaskView getInventoryTaskView() {
        var inventoryTaskView = new InventoryTaskView();
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
            }
        }, GRACE_INVULNERABLE_TIME);
    }

    public Lockout getPlugin() {
        return this.plugin;
    }
}
