package stretch.lockout.game;

import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.game.state.GameState;
import stretch.lockout.kit.CompassKit;
import stretch.lockout.platform.Platform;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.JsonUtil;
import stretch.lockout.util.MessageUtil;
import stretch.lockout.util.TimingUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class LockoutCommand implements TabExecutor {
    private final RaceGameContext lockout;
    private final FileConfiguration config;
    private final CompassKit compassKit = new CompassKit();

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

                    if (lockout.getGameStateHandler().getGameState() != GameState.READY) {
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

                    }
                    else {
                        return false;
                    }
                }
                case "compass" -> {
                    compassKit.apply(player);
                }
                case "start" -> {
                    if (lockout.gameRules().contains(GameRule.OP_COMMANDS) && !player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (lockout.getGameStateHandler().getGameState() != GameState.READY) {
                        MessageUtil.sendChat(player, "The game is already started!");
                    }
                    else {
                        lockout.getGameStateHandler().setGameState(GameState.STARTING);
                    }
                }
                case "end" -> {
                    if (lockout.gameRules().contains(GameRule.OP_COMMANDS) && !player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    lockout.getGameStateHandler().setGameState(GameState.END);
                }
                case "version" -> {
                    MessageUtil.log(sender, lockout.getPlugin().getDescription().getVersion());
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
                ImmutableList.of("team", "end", "start", "compass", "version") :
                ImmutableList.of("");
    }
}
