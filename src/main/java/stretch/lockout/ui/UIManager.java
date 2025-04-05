package stretch.lockout.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.event.*;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.state.GameState;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.task.HiddenTask;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.team.player.PlayerStat;
import stretch.lockout.ui.bar.*;
import stretch.lockout.ui.inventory.InventoryInputHandler;
import stretch.lockout.ui.misc.CountDownSequence;
import stretch.lockout.ui.scoreboard.ScoreboardHandler;
import stretch.lockout.util.LockoutLogger;

import java.util.function.Consumer;

public class UIManager implements Listener {
    private final LockoutContext lockout;
    private final BarManager barManager;
    private ScoreboardHandler scoreboardManager;
    public UIManager(final LockoutContext lockout) {
        this.lockout = lockout;
        this.scoreboardManager = new ScoreboardHandler();
        this.barManager = new BarManager(lockout);
        new InventoryInputHandler(lockout);
        Bukkit.getPluginManager().registerEvents(this, lockout.getPlugin());
    }

    public void reset() {
        barManager.destroyBars();
        scoreboardManager.resetScoreboard();
        scoreboardManager = new ScoreboardHandler();
    }

    // TODO timer should not be internal to this package
    public LockoutTimer getTimer() {
        LockoutLogger.debugLog(ChatColor.RED + "TIMER SHOULD BE SEPARATE FROM UI");
        return barManager.getTimer();
    }

    @EventHandler
    public void onReadyGame(ReadyGameEvent readyGameEvent) {
        barManager.getPreGameBar().activate();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStartGame(StartGameEvent startGameEvent) {
        barManager.getPreGameBar().deactivate();
        lockout.getTeamManager().getTeams()
                .forEach(scoreboardManager::addTeam);
        scoreboardManager.update();

        // Call countdown
        Bukkit.getScheduler().scheduleSyncDelayedTask(lockout.getPlugin(),
                new CountDownSequence(lockout, lockout.settings().getCountdownTime(),
                        lockout.getTeamManager().getPlayerStats().stream()
                                .map(PlayerStat::getPlayer)
                                .toList()), 20L);
    }

    @EventHandler
    public void onTieBreaker(TieBreakerEvent tieBreakerEvent) {
        barManager.getTimer().deactivate();
        if (!lockout.settings().hasRule(LockoutGameRule.TIE_BREAK) || !lockout.getTieBreaker().isTasksLoaded()) {
            String message = ChatColor.YELLOW + "Draw!";
            LockoutLogger.sendAllTitle(message, "", 10, 20, 10);
            LockoutLogger.sendAllChat("The game was a " + message);
            return;
        }

        barManager.getTimer().activate();
        String message = ChatColor.DARK_RED + "Tie breaker!";
        LockoutLogger.sendAllTitle(message, "", 10, 20, 10);
        LockoutLogger.sendAllActionBar(message);
        LockoutLogger.sendAllChat("Entering " + message);

        lockout.getTeamManager().doToAllPlayers(player ->
                player.playSound(player, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F));

    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        if (lockout.getTeamManager().isPlayerOnTeam(player)) {
            scoreboardManager.updatePlayer(player);
        }

        // Update bossbars
        // Should be a more efficient way to fix boss bars
        switch (lockout.getGameStateHandler().getGameState()) {
            case READY -> {
                barManager.getPreGameBar().activate();
            }
            case RUNNING -> {
                if (lockout.settings().hasRule(LockoutGameRule.TIMER)) {
                    barManager.getTimer().activate();
                }
            }
            case TIEBREAKER -> {
                if (lockout.settings().hasRule(LockoutGameRule.TIE_BREAK)) {
                    barManager.getTieBar().activate();
                }
            }
            default -> {}
        }
    }

    @EventHandler
    public void onTaskCompleted(final TaskCompletedEvent taskCompletedEvent) {
        if (taskCompletedEvent.isCancelled()) {
            return;
        }

        var task = taskCompletedEvent.getTask();
        var scoredPlayerStat = task.getScoredPlayer();

        // update board for all teams
        scoreboardManager.update();
        TeamManager teamManager = lockout.getTeamManager();
        LockoutTeam team = taskCompletedEvent.getPlayer().getTeam();

        if (!(task instanceof HiddenTask)) {
            // Play sound for all players
            LockoutLogger.debugLog(ChatColor.RED + "Fix sound in UI");

            //teamManager.doToAllPlayers(PlayerEffect.NEGATIVE_TASK::accept);
            //teamManager.doToAllPlayers(PlayerEffect.POSITIVE_TASK::accept);
            //teamManager.doToAllPlayers(PlayerEffect.TESTER::accept);
            //teamManager.doToAllPlayers(PlayerEffect.TESTERR::accept);

            LockoutTeam lockoutTeam = scoredPlayerStat.getTeam();
            String playerName = lockoutTeam.playerCount() == 1 && lockoutTeam.getName().equals(scoredPlayerStat.getPlayer().getName()) ?
                    scoredPlayerStat.getPlayer().getDisplayName() :
                    "[" + lockoutTeam.getName() + "]" + scoredPlayerStat.getPlayer().getDisplayName();

            String friendlyMessage = ChatColor.GRAY + playerName + " completed task: "
                    + ChatColor.BLUE + task.getDescription();

            String enemyMessage = ChatColor.DARK_RED + playerName + " completed task: "
                    + ChatColor.RED + task.getDescription();

            team.doToPlayers(player -> {
                LockoutLogger.sendActionBar(player, friendlyMessage);
                LockoutLogger.sendChat(player, friendlyMessage);
                //PlayerEffect.POSITIVE_TASK.accept(player);
                PlayerEffect.NEGATIVE_TASK.accept(player);
                //new PlayerAdvancementMessage(task).sendMessage(player);
            });
            team.doToOpposingTeams(player -> {
                LockoutLogger.sendActionBar(player, enemyMessage);
                LockoutLogger.sendChat(player, enemyMessage);
                PlayerEffect.NEGATIVE_TASK.accept(player);
            });
        }
    }

    @EventHandler
    public void onRewardApply(final RewardApplyEvent rewardApplyEvent) {
        PlayerStat scoredPlayerStat = rewardApplyEvent.getPlayerStat();
        RewardComponent reward = rewardApplyEvent.getReward();

        Consumer<Player> sendMessage = (player) -> {
            String messagePrefix = switch (reward.getRewardType()) {
                case SOLO -> scoredPlayerStat.getPlayer().getName() + " received: ";
                case TEAM, COMPOSITE -> "Team " + scoredPlayerStat.getTeam().getName() + " received: ";
                case ENEMY -> "Team " + scoredPlayerStat.getTeam().getName() + " caused debuff: ";
            };
            String message = messagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription();
            LockoutLogger.sendChat(player, ChatColor.GRAY + message);
        };

        scoredPlayerStat.getTeam()
            .doToPlayers(sendMessage);
        scoredPlayerStat.getTeam()
            .doToOpposingTeams(sendMessage
                               .andThen(player -> player.playSound(player, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 0.5F)));
    }

    @EventHandler
    public void onGameOver(final GameOverEvent gameOverEvent) {
        LockoutTeam winningTeam = gameOverEvent.getTeam();
        float volume = 1F;
        float pitch = 0.85F;

        winningTeam.doToPlayers(player -> {
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1, volume, pitch);
            player.sendTitle(ChatColor.GREEN + winningTeam.getName(),
                    ChatColor.GOLD + "has won!", 1, 80, 10);
        });

        winningTeam.doToOpposingTeams(player -> {
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, volume, pitch);
            player.sendTitle(ChatColor.RED + winningTeam.getName(),
                    ChatColor.GOLD + "has won!", 1, 80, 10);
        });

        // TODO not sure if it HAS to be this way.
        barManager.destroyBars();
        //destroy();
    }

    @EventHandler
    public void onPlayerJoinTeam(final PlayerJoinTeamEvent playerJoinTeamEvent) {
        var playerStat = playerJoinTeamEvent.getPlayerStat();
        LockoutLogger.sendChat(playerStat.getPlayer(), "You joined team " + ChatColor.GOLD + playerStat.getTeam().getName());
    }


    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent interactEvent) {
        Player player = interactEvent.getPlayer();

        if (interactEvent.hasItem() && interactEvent.getItem().getType() == Material.COMPASS) {
            ItemStack compass = interactEvent.getItem();
            if (compass.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                if (interactEvent.getAction() == Action.RIGHT_CLICK_AIR || interactEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    GameState gameState = lockout.getGameStateHandler().getGameState();
                    if ((!lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                            || player.hasPermission("lockout.select"))
                            && !lockout.getCurrentTaskCollection().isTasksLoaded()) {
                        player.openInventory(lockout.getTaskSelectionView().getInventory());
                    }
                    else if (lockout.getCurrentTaskCollection().isTasksLoaded()
                            && lockout.getTeamManager().isPlayerOnTeam(player)
                            && gameState != GameState.READY) {
                        player.openInventory(lockout.getInventoryTaskView().getInventory());
                    }
                    else {
                        player.openInventory(lockout.getTeamManager().getTeamSelectionView().getInventory());
                    }
                }
                if ((interactEvent.getAction() == Action.LEFT_CLICK_AIR || interactEvent.getAction() == Action.LEFT_CLICK_BLOCK)
                        && (lockout.getGameStateHandler().getGameState() == GameState.RUNNING || lockout.getGameStateHandler().getGameState() == GameState.TIEBREAKER)
                        && lockout.settings().hasRule(LockoutGameRule.COMPASS_TRACKING)) {
                    lockout.getPlayerTracker().changeTracker(player);
                }
            }
        }
    }
}
