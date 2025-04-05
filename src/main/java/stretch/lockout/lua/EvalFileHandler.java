package stretch.lockout.lua;

import org.bukkit.command.CommandSender;
import org.luaj.vm2.LuaValue;

public interface EvalFileHandler {
    void loadFile(CommandSender sender, String filePath);
    void loadFile(String filePath);
    LuaValue loadString(CommandSender sender, String data);
}
