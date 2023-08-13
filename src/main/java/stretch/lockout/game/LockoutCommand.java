package stretch.lockout.game;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.MessageUtil;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LockoutCommand implements TabExecutor {
    private final RaceGameContext lockout;
    private final FileConfiguration config;

    public LockoutCommand(final RaceGameContext lockout, final FileConfiguration config) {
        this.lockout = lockout;
        this.config = config;
    }
    private void noPermissionMessage(CommandSender sender) {
        MessageUtil.log(sender, ChatColor.RED + "You do not have permission to use that.");
    }
    @Override
    public boolean onCommand(@NonNull CommandSender sender,
                             @NonNull Command cmd,
                             @NonNull String label,
                             @NonNull String[] args) {

        if (sender instanceof Player player && (cmd.getName().equalsIgnoreCase("ready") ||
                cmd.getName().equalsIgnoreCase("unready"))) {
            if (!player.hasPermission("lockout.ready") || !config.getBoolean("allowPlayerReady")) {
                noPermissionMessage(player);
                return true;
            }

            return true;
        }

        if (sender instanceof Player player && cmd.getName().equalsIgnoreCase("lockout") && args.length >= 1) {
            String command = args[0];
            switch (command) {
                case "team" -> {
                    if (lockout.gameRules().contains(GameRule.OP_COMMANDS) && !player.hasPermission("lockout.team")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (lockout.getGameState() != GameState.READY) {
                        MessageUtil.sendChat(player, "Cannot create team; Lockout has already started.");
                        return true;
                    }

                    if (args.length == 2) {
                        String teamName = args[1];
                        TeamManager teamManager = lockout.getTeamManager();

                        if (!teamManager.isTeam(teamName)) {
                            LockoutTeam team = new LockoutTeam(teamName, teamManager.getTeamSize());

                            ItemStack handItem = player.getEquipment().getItemInMainHand();

                            if (handItem.getType() != Material.AIR) {
                                team.setGuiItem(new ItemStack(handItem));
                            }

                            if (teamManager.addTeam(team)) {
                                MessageUtil.sendAllChat(player.getName() + " created team " + ChatColor.GOLD + teamName);
                            }
                            else {
                                MessageUtil.sendChat(player, "Cannot create anymore teams. Max is " + teamManager.getMaxTeams());
                            }
                        }
                        else {
                            MessageUtil.sendChat(player, "Team already exists!");
                        }

                        if (!lockout.hasGuiCompass(player)) {
                            player.getInventory().addItem(lockout.getGuiCompass());
                        }
                    }
                    else {
                        return false;
                    }
                }
                case "compass" -> {
                    if (lockout.gameRules().contains(GameRule.OP_COMMANDS) && !player.hasPermission("lockout.compass")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    Inventory playerInv = player.getInventory();
                    playerInv.addItem(lockout.getGuiCompass());
                }
                case "start" -> {
                    if (lockout.gameRules().contains(GameRule.OP_COMMANDS) && !player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (lockout.getGameState() != GameState.READY) {
                        MessageUtil.sendChat(player, "The game is already started!");
                    }
                    else {
                        lockout.setGameState(GameState.STARTING);
                    }

                }
                case "end" -> {
                    if (lockout.gameRules().contains(GameRule.OP_COMMANDS) && !player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    lockout.setGameState(GameState.END);
                }
                case "debugteam" -> {
                    String teamName = ChatColor.AQUA + "aqua";
                    lockout.getTeamManager().createTeam(teamName);
                }
                case "debug" -> {
                    Inventory inv = lockout.getTeamManager().getTeamSelectionView().getInventory();
                    player.openInventory(inv);
                }
                default -> {return false;}
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command cmd,
            @NonNull String label,
            @NonNull String[] args) {
        return cmd.getName().equalsIgnoreCase("lockout") && args.length == 1 ?
                ImmutableList.of("team", "end", "start", "compass") :
                ImmutableList.of("");
    }
}
