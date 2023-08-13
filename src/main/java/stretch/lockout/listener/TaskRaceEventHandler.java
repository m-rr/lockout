package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import stretch.lockout.event.*;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.loot.LootManager;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.scoreboard.ScoreboardHandler;
import stretch.lockout.task.TaskInvisible;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class TaskRaceEventHandler implements Listener {
    private final RaceGameContext taskRaceContext;
    public TaskRaceEventHandler(RaceGameContext taskRaceContext) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onTaskCompleted(TaskCompletedEvent taskCompletedEvent) {
        if (taskCompletedEvent.isCancelled()) {
            return;
        }

        var task = taskCompletedEvent.getTask();
        var scoredPlayerStat = task.getScoredPlayer();

        // update board for all teams
        taskRaceContext.getScoreboardManager().update();
        TeamManager teamManager = taskRaceContext.getTeamManager();
        LockoutTeam team = taskCompletedEvent.getPlayer().getTeam();

        if (!(task instanceof TaskInvisible)) {
            // Play sound for all players
            teamManager.doToAllPlayers(player -> {
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
            });

            LockoutTeam lockoutTeam = scoredPlayerStat.getTeam();
            String playerName = lockoutTeam.playerCount() == 1 && lockoutTeam.getName().equals(scoredPlayerStat.getPlayer().getName()) ?
                    scoredPlayerStat.getPlayer().getDisplayName() :
                    "[" + lockoutTeam.getName() + "]" + scoredPlayerStat.getPlayer().getDisplayName();

            String friendlyMessage = ChatColor.GRAY + playerName + " completed task: "
                    + ChatColor.BLUE + task.getDescription();

            String enemyMessage = ChatColor.DARK_RED + playerName + " completed task: "
                    + ChatColor.RED + task.getDescription();

            team.doToPlayers(player -> {
                MessageUtil.sendActionBar(player, friendlyMessage);
                MessageUtil.sendChat(player, friendlyMessage);
            });
            team.doToOpposingTeams(player -> {
                MessageUtil.sendActionBar(player, enemyMessage);
                MessageUtil.sendChat(player, enemyMessage);
            });
        }

        // apply rewards
        if (task.hasReward() && taskRaceContext.gameRules().contains(GameRule.ALLOW_REWARD)) {
            var reward = task.getReward();
            reward.applyReward(scoredPlayerStat);
            if (!(task instanceof TaskInvisible)) {
                var rewardEvent = new RewardApplyEvent(scoredPlayerStat, reward);
                Bukkit.getPluginManager().callEvent(rewardEvent);
            }

            var actions = reward.getActions();
            if (!actions.isEmpty()) {
                taskRaceContext.getRewardScheduler().scheduleRewardActions(reward);
            }
        }

        Predicate<RaceGameContext> isGameOver = taskRaceContext.gameRules().contains(GameRule.MAX_SCORE) ?
                (game) -> game.getMaxScore() > 0 && team.getScore() >= game.getMaxScore() :
                (game) -> (long) game.getTeamManager().getTeams().size() > 1 && game.getTeamManager().getOpposingTeams(team).stream()
                        .noneMatch(teams -> game.getTaskManager().remainingPoints() + teams.getScore() >= team.getScore());


        if (isGameOver.test(taskRaceContext)) {
          Bukkit.getPluginManager().callEvent(new GameOverEvent(team));
          return;
        }

        LootManager lootManager = taskRaceContext.getLootManager();
        if (taskRaceContext.gameRules().contains(GameRule.SPAWN_LOOT) &&
                Math.random() <= lootManager.getLootSpawnChance()) {

            if (taskRaceContext.gameRules().contains(GameRule.LOOT_NEAR)) {
                lootManager.spawnNearPlayer(scoredPlayerStat.getPlayer());
            }
            else {
                lootManager.spawnLootBorder();
            }
        }
    }

    @EventHandler
    public void onRewardApply(RewardApplyEvent rewardApplyEvent) {
        PlayerStat scoredPlayerStat = rewardApplyEvent.getPlayerStat();
        RewardComponent reward = rewardApplyEvent.getReward();

        Consumer<Player> sendMessage = (player) -> {
            String messagePrefix = switch (reward.getRewardType()) {
                case SOLO -> scoredPlayerStat.getPlayer().getName() + " received: ";
                case TEAM, COMPOSITE -> "Team " + scoredPlayerStat.getTeam().getName() + " received: ";
                case ENEMY -> "Team " + scoredPlayerStat.getTeam().getName() + " caused debuff: ";
            };
            String message = messagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription();
            MessageUtil.sendChat(player, ChatColor.GRAY + message);
        };

        scoredPlayerStat.getTeam().doToPlayers(sendMessage);
        scoredPlayerStat.getTeam().doToOpposingTeams(sendMessage.andThen(player -> player.playSound(player, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 0.5F)));
    }

    @EventHandler
    public void onStartGame(StartGameEvent startGameEvent) {
        // Set scoreboard for players
        var teams = taskRaceContext.getTeamManager().getTeams();
        teams.removeIf(lockoutTeam -> lockoutTeam.playerCount() < 1);
        ScoreboardHandler scoreboardHandler = taskRaceContext.getScoreboardManager();
        teams.forEach(scoreboardHandler::addTeam);
        scoreboardHandler.update();
    }

    @EventHandler
    public void onGameOver(GameOverEvent gameOverEvent) {
        if (gameOverEvent.isCancelled()) {
            return;
        }

        taskRaceContext.setGameState(GameState.PAUSED);
        var winningTeam = gameOverEvent.getTeam();
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

       Bukkit.getScheduler().scheduleSyncDelayedTask(taskRaceContext.getPlugin(), () -> taskRaceContext.setGameState(GameState.END), 100);
    }

    @EventHandler
    public void onPlayerJoinTeam(PlayerJoinTeamEvent playerJoinTeamEvent) {
        var playerStat = playerJoinTeamEvent.getPlayerStat();
        MessageUtil.sendChat(playerStat.getPlayer(), "You joined team " + ChatColor.GOLD + playerStat.getTeam().getName());
    }

    @EventHandler
    public void onResetGame(ResetGameEvent resetGameEvent) {
        taskRaceContext.setGameState(GameState.END);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent interactEvent) {
        var player = interactEvent.getPlayer();
        if (interactEvent.hasItem() && interactEvent.getItem().getType() == Material.COMPASS) {
            var compass = interactEvent.getItem();
            if (compass.getEnchantments().containsKey(Enchantment.LUCK)) {
                if (interactEvent.getAction() == Action.RIGHT_CLICK_AIR || interactEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    GameState gameState = taskRaceContext.getGameState();
                    if ((!taskRaceContext.gameRules().contains(GameRule.OP_COMMANDS)
                            || player.hasPermission("lockout.select"))
                            && !taskRaceContext.getTaskManager().isTasksLoaded()) {
                        player.openInventory(taskRaceContext.getTaskSelectionView().getInventory());
                    }
                    else if (taskRaceContext.getTaskManager().isTasksLoaded()
                            && taskRaceContext.getTeamManager().isPlayerOnTeam(player)
                            && gameState != GameState.READY) {
                        player.openInventory(taskRaceContext.getInventoryTaskView().getInventory());
                    }
                    else {
                        player.openInventory(taskRaceContext.getTeamManager().getTeamSelectionView().getInventory());
                    }
                }
                if ((interactEvent.getAction() == Action.LEFT_CLICK_AIR || interactEvent.getAction() == Action.LEFT_CLICK_BLOCK)
                        && taskRaceContext.getGameState() == GameState.RUNNING && taskRaceContext.gameRules().contains(GameRule.COMPASS_TRACKING)) {
                    taskRaceContext.getPlayerTracker().changeTracker(player);
                }
            }
        }
    }
}
