package stretch.lockout.game;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.luaj.vm2.LuaValue;
import stretch.lockout.Lockout;
import stretch.lockout.game.state.GameState;
import stretch.lockout.game.state.GameStateHandler;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.kit.CompassKit;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.TeamManager;
import stretch.lockout.util.LockoutLogger;

import java.util.List;
import java.util.Objects;

public class LockoutCommand implements TabExecutor {

    private final Plugin plugin;
    private final CompassKit compassKit;
    private LockoutSettings settings;
    private final GameStateHandler stateHandler;;
    private final TeamManager teamManager;
    private final LuaEnvironment userLuaEnvironment;

    public LockoutCommand(@NonNull Plugin plugin,
                          @NonNull CompassKit compassKit,
                          @NonNull LockoutSettings settings,
                          @NonNull GameStateHandler stateHandler,
                          @NonNull TeamManager teamManager,
                          @NonNull LuaEnvironment userLuaEnvironment) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.compassKit = Objects.requireNonNull(compassKit, "compassKit cannot be null");
        this.settings = Objects.requireNonNull(settings, "settings cannot be null");
        this.stateHandler = Objects.requireNonNull(stateHandler, "stateHandler cannot be null");
        this.teamManager = Objects.requireNonNull(teamManager, "teamManager cannot be null");
        this.userLuaEnvironment = Objects.requireNonNull(userLuaEnvironment, "userLuaEnvironment cannot be null");
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
                        if (settings.hasRule(LockoutGameRule.OP_COMMANDS) && !player.hasPermission("lockout.team")) {
                            noPermissionMessage(player);
                            return true;
                        }

                        if (stateHandler.getGameState() != GameState.READY) {
                            LockoutLogger.sendChat(player, "Cannot create team; Lockout has already started.");
                            return true;
                        }

                        if (args.length == 2) {
                            String teamName = args[1];

                            if (!teamManager.isTeam(teamName)) {
                                LockoutTeam team = new LockoutTeam(teamName, settings.getTeamSize());

                                ItemStack handItem = player.getEquipment().getItemInMainHand();

                                if (handItem.getType() != Material.AIR) {
                                    team.setGuiItem(new ItemStack(handItem));
                                }

                                if (teamManager.addTeam(team)) {
                                    LockoutLogger.sendAllChat(player.getName() + " created team " + ChatColor.GOLD + teamName);
                                }
                                else {
                                    LockoutLogger.sendChat(player, "Cannot create anymore teams. Max is " + settings.getMaxTeams());
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
                        if (settings.hasRule(LockoutGameRule.DEV)) {
                            return false;
                        }


                    }
                    case "start" -> {
                    if (settings.hasRule(LockoutGameRule.OP_COMMANDS)
                        && !player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (stateHandler.getGameState() != GameState.READY) {
                        LockoutLogger.log(sender, "The game is already started!");
                    }
                    else {
                        stateHandler.setGameState(GameState.STARTING);
                    }
                }
                case "end" -> {
                    if (settings.hasRule(LockoutGameRule.OP_COMMANDS)
                        && !player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    stateHandler.setGameState(GameState.END);
                }
                case "version" -> {
                    LockoutLogger.log(sender, plugin.getPluginMeta().getVersion());
                }
                case "eval" -> {
                    if (settings.hasRule(LockoutGameRule.DEV)) {
                        final int length = args.length;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 1; i < length; i++) {
                            stringBuilder.append(args[i]).append(" ");
                        }
                        String chunk = stringBuilder.toString();
                        LuaValue rt = userLuaEnvironment.loadString(sender, chunk);
                        LockoutLogger.evalLog(sender, ChatColor.GREEN + chunk);
                        LockoutLogger.evalLog(sender, rt.toString());
                    }
                    else {
                        LockoutLogger.log(sender, "You are not in dev mode");
                    }
                }
                case "reload" -> {
                    if (settings.hasRule(LockoutGameRule.OP_COMMANDS)
                        && !player.hasPermission("lockout.reload")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    LockoutLogger.debugLog(ChatColor.RED + "Reload request from " + sender.getName());
                    stateHandler.setGameState(GameState.PAUSED);
                    LockoutSettings oldSettings = settings;
                    //lockout.updateSettings(lockout.getPlugin().generateConfig(true));
                    Lockout plugin = (Lockout) Bukkit.getPluginManager().getPlugin("Lockout");
                    assert plugin != null;
                    LockoutContext lockout = plugin.getLockoutContext();
                    LockoutSettings newSettings = plugin.generateConfig(true);
                    this.settings = newSettings;
                    lockout.updateSettings(newSettings);
                    newSettings.showDiff(oldSettings);

                    userLuaEnvironment.reset();
                    if (settings.hasRule(LockoutGameRule.DEV)) {
                        userLuaEnvironment.loadUserInitScript();
                    }

                    lockout.getBoardManager().reset();
                    lockout.getBoardManager().registerBoardsAsync();

                    lockout.getGameStateHandler().setGameState(GameState.END);
                    LockoutLogger.debugLog(ChatColor.GREEN + "Reload complete");
                }
                    default -> {return false;}
                }
            } else {
                switch (command) {
                case "start" -> {
                    if (settings.hasRule(LockoutGameRule.OP_COMMANDS)
                        && sender instanceof Player player
                        && !player.hasPermission("lockout.start")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    if (stateHandler.getGameState() != GameState.READY) {
                        LockoutLogger.log(sender, "The game is already started!");
                    }
                    else {
                        stateHandler.setGameState(GameState.STARTING);
                    }
                }
                case "end" -> {
                    if (settings.hasRule(LockoutGameRule.OP_COMMANDS)
                        && sender instanceof Player player
                        && !player.hasPermission("lockout.end")) {
                        noPermissionMessage(player);
                        return true;
                    }
                    stateHandler.setGameState(GameState.END);
                }
                case "version" -> {
                    LockoutLogger.log(sender, plugin.getDescription().getVersion());
                }
                case "eval" -> {
                    if (settings.hasRule(LockoutGameRule.DEV)) {
                        final int length = args.length;
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 1; i < length; i++) {
                            stringBuilder.append(args[i]).append(" ");
                        }
                        String chunk = stringBuilder.toString();
                        LuaValue rt = userLuaEnvironment.loadString(sender, chunk);
                        LockoutLogger.evalLog(sender, rt.toString());
                    }
                    else {
                        LockoutLogger.log(sender, "You are not in dev mode");
                    }
                }
                case "reload" -> {
                    if (settings.hasRule(LockoutGameRule.OP_COMMANDS)
                        && sender instanceof Player player
                        && !player.hasPermission("lockout.reload")) {
                        noPermissionMessage(player);
                        return true;
                    }

                    LockoutLogger.debugLog(ChatColor.RED + "Reload request from " + sender.getName());
                    stateHandler.setGameState(GameState.PAUSED);
                    LockoutSettings oldSettings = settings;
                    //lockout.updateSettings(lockout.getPlugin().generateConfig(true));
                    Lockout plugin = (Lockout) Bukkit.getPluginManager().getPlugin("Lockout");
                    assert plugin != null;
                    LockoutContext lockout = plugin.getLockoutContext();
                    LockoutSettings newSettings = plugin.generateConfig(true);
                    lockout.updateSettings(plugin.generateConfig(true));
                    lockout.settings().showDiff(oldSettings);

                    userLuaEnvironment.reset();
                    userLuaEnvironment.loadUserInitScript();

                    lockout.getBoardManager().reset();
                    lockout.getBoardManager().registerBoardsAsync();

                    stateHandler.setGameState(GameState.END);
                    LockoutLogger.debugLog(ChatColor.GREEN + "Reload complete");
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
