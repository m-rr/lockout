package stretch.lockout.lua;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.lua.table.*;
import stretch.lockout.util.MessageUtil;

public class LuaEnvironment extends LuaFileHandler {
    private final Globals table;
    private final RaceGameContext lockout;
    private final String dataPath = "plugins/Lockout/";
    private final String fileExtension = ".tasks";

    public LuaEnvironment(final RaceGameContext lockout) {
        this.lockout = lockout;
        this.table = JsePlatform.standardGlobals();
        initTable();
    }

    public void loadFile(CommandSender sender, String filePath) {
        LuaValue chunk = table.loadfile(dataPath + filePath + fileExtension);
        chunk.call();
        MessageUtil.log(sender, "Tasks from " + filePath + " loaded.");
    }

    public void loadFile(String filePath) {
        loadFile(Bukkit.getConsoleSender(), filePath);
    }

    private void initTable() {

        ImmutableList.of(
                new LuaRewardBindings(lockout),
                new LuaClassBindings(lockout),
                new LuaHelperBindings(lockout),
                new LuaTaskBindings(),
                new LuaPredicateBindings())
                .forEach(luaTableBinding -> luaTableBinding.injectBindings(table));
    }

}
