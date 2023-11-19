package stretch.lockout.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;
import stretch.lockout.game.GameRule;
import stretch.lockout.game.RaceGameContext;

public class MessageUtil {
    private static void sendAll(ChatMessageType chatMessageType, String message) {
        TextComponent content = new TextComponent(message);
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(chatMessageType, content));
    }

    public static void sendAllActionBar(String message) {
        sendAll(ChatMessageType.ACTION_BAR, message);
    }

    public static void sendAllChat(String message) {
        sendAll(ChatMessageType.CHAT, fancify(message));
    }

    private static void send(Player player, ChatMessageType chatMessageType, String message) {
        TextComponent content = new TextComponent(message);
        player.spigot().sendMessage(chatMessageType, content);
    }

    public static void sendChat(Player player, String message) {
        send(player, ChatMessageType.CHAT, fancify(message));
    }

    public static void sendLink(Player player, String url, String message) {
        TextComponent content = new TextComponent(fancify(message));
        content.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        player.spigot().sendMessage(ChatMessageType.CHAT, content);
    }

    public static void sendActionBar(Player player, String message) {
        send(player, ChatMessageType.ACTION_BAR, message);
    }

    public static void consoleLog(String message) {
        log(Bukkit.getConsoleSender(), message);
    }

    public static void debugLog(final RaceGameContext lockout, String message) {
        if (lockout.gameRules().contains(GameRule.DEBUG)) {
            consoleLog(message);
            Bukkit.getOnlinePlayers().stream()
                    .filter(Player::isOp)
                    .forEach(player -> MessageUtil.sendChat(player, message));
        }
    }

    public static void log(CommandSender sender, String message) {
        sender.sendMessage(fancify(message));
    }

    private static String fancify(String message) {
        return ChatColor.BLACK + "[" + ChatColor.GOLD + "Lockout" + ChatColor.BLACK + "] " + ChatColor.DARK_GRAY + message;
    }
}
