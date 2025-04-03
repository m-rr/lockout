package stretch.lockout.lua;

import org.bukkit.command.CommandSender;

public interface EvalFileHandler {
    void loadFile(CommandSender sender, String filePath);
    void loadFile(String filePath);
    void loadString(CommandSender sender, String data);
}
