package stretch.lockout.listener;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import stretch.lockout.event.*;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.loot.LootManager;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.task.TaskInvisible;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;
import stretch.lockout.util.WorldUtil;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class TaskRaceEventHandler implements Listener {
    private final RaceGameContext taskRaceContext;
    public TaskRaceEventHandler(RaceGameContext taskRaceContext) {
        this.taskRaceContext = taskRaceContext;
        Bukkit.getPluginManager().registerEvents(this, taskRaceContext.getPlugin());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {

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

            String friendlyMessage = ChatColor.GRAY + scoredPlayerStat.getTeam().getName() + " completed task: "
                    + ChatColor.BLUE + task.getDescription();

            String enemyMessage = ChatColor.DARK_RED + scoredPlayerStat.getTeam().getName() + " completed task: "
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
        else {
            Player player = scoredPlayerStat.getPlayer();
            player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_STEP, 1F, .8F);
        }

        // apply rewards
        if (task.hasReward() && taskRaceContext.gameRules().contains(GameRule.ALLOW_REWARD)) {
            var reward = task.getReward();
            reward.applyReward(scoredPlayerStat);
            if (!(task instanceof TaskInvisible)) {
                var rewardEvent = new RewardApplyEvent(scoredPlayerStat, reward);
                Bukkit.getPluginManager().callEvent(rewardEvent);
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
                case POSITIVE -> scoredPlayerStat.getPlayer().getName() + " received: ";
                case TEAM_POSITIVE, COMPOSITE -> "Team " + scoredPlayerStat.getTeam().getName() + " received: ";
                case ENEMY_NEGATIVE -> "Team " + scoredPlayerStat.getTeam().getName() + " caused debuff: ";
            };
            String message = messagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription();
            MessageUtil.sendChat(player, ChatColor.GRAY + message);
        };

        scoredPlayerStat.getTeam().doToPlayers(sendMessage);
        scoredPlayerStat.getTeam().doToOpposingTeams(sendMessage.andThen(player -> player.playSound(player, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 0.5F)));
    }

    @EventHandler
    public void onStartGame(StartGameEvent startGameEvent) {

    }

    @EventHandler
    public void onGameOver(GameOverEvent gameOverEvent) {
        if (gameOverEvent.isCancelled()) {
            return;
        }

        //taskRaceContext.pauseGame();
        taskRaceContext.setGameState(GameState.PAUSED);
        var winningTeam = gameOverEvent.getTeam();

       taskRaceContext.getTeamManager().getTeams()
               .forEach(team -> {
                   ChatColor chatColor = ChatColor.RED;
                   Sound gameOverSound = Sound.ITEM_GOAT_HORN_SOUND_7;
                   if (team == winningTeam) {
                       chatColor = ChatColor.GREEN;
                       gameOverSound = Sound.ITEM_GOAT_HORN_SOUND_1;
                   }
                   Sound finalGameOverSound = gameOverSound;
                   //team.sendMessage(chatColor + "Team " + winningTeam.getName() + " has won!");
                   //team.doToPlayers(player -> player.playSound(player, finalGameOverSound, 1F, 0.85F));
                   ChatColor finalChatColor = chatColor;
                   team.doToPlayers(player -> {
                       player.playSound(player, finalGameOverSound, 1F, 0.85F);
                       player.sendTitle(finalChatColor + winningTeam.getName(),  ChatColor.GOLD + "has won!", 1, 80, 10);
                   });
               });

       Bukkit.getScheduler().scheduleSyncDelayedTask(taskRaceContext.getPlugin(), () -> taskRaceContext.setGameState(GameState.END), 100);
    }

    @EventHandler
    public void onPlayerJoinTeam(PlayerJoinTeamEvent playerJoinTeamEvent) {
        var playerStat = playerJoinTeamEvent.getPlayerStat();
        // set scoreboard for players
        taskRaceContext.getScoreboardManager().addTeam(playerStat.getTeam());
        taskRaceContext.getScoreboardManager().update();
        MessageUtil.sendAllChat(playerStat.getPlayer().getName() + " joined team " + ChatColor.GOLD + playerStat.getTeam().getName());
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
                    if (taskRaceContext.getTaskManager().isTasksLoaded()) {
                        player.openInventory(taskRaceContext.getInventoryTaskView().getInventory());
                    }
                    else {
                        player.openInventory(taskRaceContext.getTaskSelectionView().getInventory());
                    }
                }
                if ((interactEvent.getAction() == Action.LEFT_CLICK_AIR || interactEvent.getAction() == Action.LEFT_CLICK_BLOCK)
                        && taskRaceContext.getGameState() == GameState.RUNNING && taskRaceContext.gameRules().contains(GameRule.COMPASS_TRACKING)) {
                    taskRaceContext.getPlayerTracker().changeTracker(player);
                }
            }
        }
    }
//&& (interactEvent.getAction() == Action.RIGHT_CLICK_AIR ||
           // interactEvent.getAction() == Action.RIGHT_CLICK_BLOCK)
}
