package stretch.lockout.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import stretch.lockout.event.*;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.loot.LootManager;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;

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

        // Send message to all players and play sound for all players
        teamManager.doToAllPlayers(player -> {
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        });

        MessageUtil.sendAllActionBar(ChatColor.GRAY + scoredPlayerStat.getTeam().getName() + " completed task: "
                + ChatColor.BLUE + task.getDescription());

        // apply rewards
        if (task.hasReward() && taskRaceContext.gameRules().contains(GameRule.ALLOW_REWARD)) {
            var reward = task.getReward();
            reward.applyReward(scoredPlayerStat);
            var rewardEvent = new RewardApplyEvent(scoredPlayerStat, reward);
            Bukkit.getPluginManager().callEvent(rewardEvent);
        }


        LockoutTeam team = taskCompletedEvent.getPlayer().getTeam();

        //int remainingPoints = taskRaceContext.getTaskManager().remainingPoints();
        // If max score is not set, then check if it is possible for other team to come back
        //if (taskRaceContext.getMaxScore() > 0 && team.getScore() >= taskRaceContext.getMaxScore()) {
        //    Bukkit.getPluginManager().callEvent(new GameOverEvent(team));
        //    return;
        //}

        Predicate<RaceGameContext> isGameOver = taskRaceContext.gameRules().contains(GameRule.MAX_SCORE) ?
                (game) -> game.getMaxScore() > 0 && team.getScore() >= game.getMaxScore() :
                (game) -> (game.getTaskManager().remainingPoints() + team.getScore()) < game.getTeamManager().getWinningTeam().getScore();

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
        var scoredPlayerStat = rewardApplyEvent.getPlayerStat();
        var reward = rewardApplyEvent.getReward();
        String messagePrefix = "";
        switch (reward.getRewardType()) {
            case POSITIVE -> messagePrefix = scoredPlayerStat.getPlayer().getName() + " received: ";
            case TEAM_POSITIVE -> messagePrefix = "Team " + scoredPlayerStat.getTeam().getName() + " received: ";
            case ENEMY_NEGATIVE -> messagePrefix = "Team " + scoredPlayerStat.getTeam().getName() + " caused debuff: ";
        }

        String finalMessagePrefix = messagePrefix;

        taskRaceContext.getTeamManager().getTeams().forEach(team -> {
            ChatColor nameColor = ChatColor.GREEN;
            if (team != scoredPlayerStat.getTeam()) {
                nameColor = ChatColor.RED;
                team.doToPlayers(player -> player.playSound(player, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 0.5F));
            }
            //team.sendMessage(nameColor + finalMessagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription());
            //ChatColor finalNameColor = nameColor;
            //team.doToPlayers(player -> taskRaceContext.getPlugin().consoleLogMessage(player, finalNameColor +
              //      finalMessagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription()));
            //team.doToPlayers(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(finalNameColor +
              //      finalMessagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription())));
            //MessageUtil.sendAllChat(finalNameColor + finalMessagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription());
        });
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
