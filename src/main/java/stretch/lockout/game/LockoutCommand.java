package stretch.lockout.game;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.kit.CompassKit;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.LockoutLogger;
import stretch.lockout.world.AsyncChunkSearcher;

import java.util.List;

public class LockoutCommand implements TabExecutor {
    private final LockoutContext lockout;
    private final CompassKit compassKit = new CompassKit();

    public LockoutCommand(final LockoutContext lockout) {
        this.lockout = lockout;
    }
    private void noPermissionMessage(CommandSender sender) {
        LockoutLogger.log(sender, ChatColor.RED + "You do not have permission to use that.");
    }
    @Override
    public boolean onCommand(@NonNull CommandSender sender,
                             @NonNull Command cmd,
                             @NonNull String label,
                             @NonNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("lockout") && args.length >= 1) {
            String command = args[0];
            if (sender instanceof Player player) {
                switch (command) {
                    case "team" -> {
                        if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS) && !player.hasPermission("lockout.team")) {
                            noPermissionMessage(player);
                            return true;
                        }

                        if (lockout.getGameStateHandler().getGameState() != GameState.READY) {
                            LockoutLogger.sendChat(player, "Cannot create team; Lockout has already started.");
                            return true;
                        }

                        if (args.length == 2) {
                            String teamName = args[1];
                            TeamManager teamManager = lockout.getTeamManager();

                            if (!teamManager.isTeam(teamName)) {
                                LockoutTeam team = new LockoutTeam(teamName, lockout.settings().getTeamSize());

                                ItemStack handItem = player.getEquipment().getItemInMainHand();

                                if (handItem.getType() != Material.AIR) {
                                    team.setGuiItem(new ItemStack(handItem));
                                }

                                if (teamManager.addTeam(team)) {
                                    LockoutLogger.sendAllChat(player.getName() + " created team " + ChatColor.GOLD + teamName);
                                }
                                else {
                                    LockoutLogger.sendChat(player, "Cannot create anymore teams. Max is " + lockout.settings().getMaxTeams());
                                }
                            }
                            else {
                                LockoutLogger.sendChat(player, "Team already exists!");
                            }

                        }
                        else {
                            return false;
                        }
                    }
                    case "compass" -> {
                        compassKit.apply(player);
                    }
                    case "dev" -> {
                        if (!lockout.settings().hasRule(LockoutGameRule.DEV)) {
                            return false;
                        }

                        AsyncChunkSearcher chunkSearcher = new AsyncChunkSearcher(lockout.getPlugin(), player.getWorld(), 55, 200);
                        // Search parameters
                        Location centerLocation = player.getLocation();
                        int searchRadius = Integer.parseInt(args[1]); // chunks
                        Material targetMaterial = Material.getMaterial(args[2]);
                        player.sendMessage(ChatColor.BLUE + "Searching for " + targetMaterial + " at " + centerLocation + " with radius of " + (searchRadius * 16) + " blocks");
                        // Start the search process
                        // Will load any unloaded chunks automatically
                        chunkSearcher.findMaterialNear(player.getLocation(), searchRadius, targetMaterial, 8).thenAccept(locations -> {
                            if (locations.isEmpty()) {
                                player.sendMessage(ChatColor.RED + "Cannot find any " + targetMaterial + " at " + centerLocation);
                            }
                            else {
                                player.sendMessage(ChatColor.GOLD + locations.toString());
                                player.sendMessage(ChatColor.GREEN + "Found " + locations.size() + " " + targetMaterial);
                            }
                        });
                    }
                    case "start" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                        && !player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (lockout.getGameStateHandler().getGameState() != GameState.READY) {
                        LockoutLogger.log(sender, "The game is already started!");
                    }
                    else {
                        lockout.getGameStateHandler().setGameState(GameState.STARTING);
                    }
                }
                case "end" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                        && !player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    lockout.getGameStateHandler().setGameState(GameState.END);
                }
                case "version" -> {
                    LockoutLogger.log(sender, lockout.getPlugin().getDescription().getVersion());
                }
                case "eval" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.DEV)) {
                        final int length = args.length;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 1; i < length; i++) {
                            stringBuilder.append(args[i]).append(" ");
                        }
                        String chunk = stringBuilder.toString();
                        lockout.getUserLuaEnvironment().loadString(sender, chunk);
                    }
                    else {
                        LockoutLogger.log(sender, "You are not in dev mode");
                    }

                }
                case "reload" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                        && !player.hasPermission("lockout.reload")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    LockoutLogger.debugLog(lockout.settings(), ChatColor.RED + "Reload request from " + sender.getName());
                    lockout.getGameStateHandler().setGameState(GameState.PAUSED);
                    LockoutSettings oldSettings = lockout.settings();
                    lockout.updateSettings(lockout.getPlugin().generateConfig(true));
                    lockout.settings().showDiff(oldSettings);

                    lockout.getUserLuaEnvironment().resetTables();
                    lockout.getUserLuaEnvironment().initUserChunk();

                    lockout.getBoardManager().reset();
                    lockout.getBoardManager().registerBoardsAsync();

                    lockout.getGameStateHandler().setGameState(GameState.END);
                    LockoutLogger.debugLog(lockout.settings(), ChatColor.GREEN + "Reload complete");
                }
                    default -> {return false;}
                }
            } else {
                switch (command) {
                case "start" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                        && sender instanceof Player player
                        && !player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (lockout.getGameStateHandler().getGameState() != GameState.READY) {
                        LockoutLogger.log(sender, "The game is already started!");
                    }
                    else {
                        lockout.getGameStateHandler().setGameState(GameState.STARTING);
                    }
                }
                case "end" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                        && sender instanceof Player player
                        && !player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    lockout.getGameStateHandler().setGameState(GameState.END);
                }
                case "version" -> {
                    LockoutLogger.log(sender, lockout.getPlugin().getDescription().getVersion());
                }
                case "reload" -> {
                    if (lockout.settings().hasRule(LockoutGameRule.OP_COMMANDS)
                        && sender instanceof Player player
                        && !player.hasPermission("lockout.reload")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    LockoutLogger.debugLog(lockout.settings(), ChatColor.RED + "Reload request from " + sender.getName());
                    lockout.getGameStateHandler().setGameState(GameState.PAUSED);
                    LockoutSettings oldSettings = lockout.settings();
                    lockout.updateSettings(lockout.getPlugin().generateConfig(true));
                    lockout.settings().showDiff(oldSettings);

                    lockout.getUserLuaEnvironment().resetTables();
                    lockout.getUserLuaEnvironment().initUserChunk();

                    lockout.getBoardManager().reset();
                    lockout.getBoardManager().registerBoardsAsync();

                    lockout.getGameStateHandler().setGameState(GameState.END);
                    LockoutLogger.debugLog(lockout.settings(), ChatColor.GREEN + "Reload complete");
                }
                default -> {return false;}
            }
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
