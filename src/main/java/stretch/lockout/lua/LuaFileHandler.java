package stretch.lockout.lua;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public abstract class LuaFileHandler {
    abstract void loadFile(CommandSender sender, String filePath);

    public void loadFile(String filePath) {
        loadFile(Bukkit.getConsoleSender(), filePath);
    }
}
