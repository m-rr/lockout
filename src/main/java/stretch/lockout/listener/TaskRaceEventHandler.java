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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import stretch.lockout.event.*;
import stretch.lockout.event.debug.DebugEvent;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.GameState;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.loot.LootManager;
import stretch.lockout.reward.RewardAction;
import stretch.lockout.reward.RewardComposite;
import stretch.lockout.reward.RewardPotion;
import stretch.lockout.reward.RewardType;
import stretch.lockout.reward.function.RewardBoomLightning;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;

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

        // update board for all teams
        //taskRaceContext.updateScoreboard();
        taskRaceContext.getScoreboardManager().update();
        TeamManager teamManager = taskRaceContext.getTeamManager();

        // Send message to all players and play sound for all players
        teamManager.doToAllPlayers(player -> {
            //player.sendRawMessage(
              //      taskCompletedEvent.getPlayer().getPlayer().getName() + " completed task: " + ChatColor.BLUE + taskCompletedEvent.getTask().getDescription());
            taskRaceContext.getPlugin().consoleLogMessage(player, taskCompletedEvent.getPlayer().getPlayer().getName() +
                    " completed task: " + ChatColor.BLUE + taskCompletedEvent.getTask().getDescription());
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        });

        var task = taskCompletedEvent.getTask();
        var scoredPlayerStat = task.getScoredPlayer();

        // apply rewards
        if (task.hasReward()) {
            var reward = task.getReward();
            reward.applyReward(scoredPlayerStat);
            var rewardEvent = new RewardApplyEvent(scoredPlayerStat, reward);
            Bukkit.getPluginManager().callEvent(rewardEvent);
        }


        LockoutTeam team = taskCompletedEvent.getPlayer().getTeam();

        int remainingPoints = taskRaceContext.getTaskManager().remainingPoints();
        // If max score is not set, then check if it is possible for other team to come back
        if (taskRaceContext.getMaxScore() > 0 && team.getScore() >= taskRaceContext.getMaxScore()) {
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
            ChatColor finalNameColor = nameColor;
            team.doToPlayers(player -> taskRaceContext.getPlugin().consoleLogMessage(player, finalNameColor +
                    finalMessagePrefix + ChatColor.LIGHT_PURPLE + reward.getDescription()));
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
        taskRaceContext.getTeamManager().doToAllPlayers(player -> {
            taskRaceContext.getPlugin().consoleLogMessage(player, playerStat.getPlayer().getName() + " joined team " + ChatColor.GOLD + playerStat.getTeam().getName());
        });
    }

    @EventHandler
    public void onResetGame(ResetGameEvent resetGameEvent) {
        taskRaceContext.setGameState(GameState.END);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent interactEvent) {
        var player = interactEvent.getPlayer();
        if (interactEvent.hasItem()
                && interactEvent.getItem().getType() == Material.COMPASS
                && (interactEvent.getAction() == Action.RIGHT_CLICK_AIR ||
                interactEvent.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            var compass = interactEvent.getItem();
            if (compass.getEnchantments().containsKey(Enchantment.LUCK)) {
                //switch (taskRaceContext.getGameState()) {
                    //case PRE -> player.openInventory(taskRaceContext.getTaskSelectionView().getInventory());
                  //  case READY, STARTING, PAUSED, RUNNING -> player.openInventory(taskRaceContext.getInventoryTaskView().getInventory());
                //}
                if (taskRaceContext.getTaskManager().isTasksLoaded()) {
                    player.openInventory(taskRaceContext.getInventoryTaskView().getInventory());
                }
                else {
                    player.openInventory(taskRaceContext.getTaskSelectionView().getInventory());
                }
            }
        }
    }

    @EventHandler
    public void onDebug(DebugEvent debugEvent) {
        RewardComposite reward = new RewardComposite();
        RewardPotion blindness = new RewardPotion(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0), RewardType.ENEMY_NEGATIVE, "Blindness");
        RewardPotion speed = new RewardPotion(new PotionEffect(PotionEffectType.SLOW, 100, 1), RewardType.TEAM_POSITIVE, "Slowness II");
        RewardAction tnt = new RewardAction(new RewardBoomLightning(), "Boom lightning");
        //reward.addReward(tnt);
        reward.addReward(speed);
        //reward.addReward(blindness);

        TeamManager teamManager = taskRaceContext.getTeamManager();

        teamManager.createTeam("debug");
        var team = teamManager.getTeamByName("debug");

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player, Sound.ENTITY_PHANTOM_SWOOP, 1F, 1F);
        });

    }
}
