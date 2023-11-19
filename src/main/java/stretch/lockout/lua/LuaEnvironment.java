package stretch.lockout.lua;

import com.google.common.collect.ImmutableList;
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
    private final String DATA_PATH = "plugins/Lockout/";
    private final String FILE_EXTENSION = ".tasks";

    public LuaEnvironment(final RaceGameContext lockout) {
        this.lockout = lockout;
        this.table = JsePlatform.standardGlobals();
        initTable();
    }

    public void loadFile(CommandSender sender, String filePath) {
        //LuaValue chunk = table.loadfile(dataPath + filePath + fileExtension);
        //chunk.call();
        //if (sender != null) {
        //    MessageUtil.log(sender, "Tasks from " + filePath + " loaded.");
        //}
        loadFile(filePath);
        MessageUtil.log(sender, "Tasks from " + filePath + " loaded.");
    }

    public void loadFile(String filePath) {
        //CommandSender sender = lockout.gameRules().contains(GameRule.DEBUG) ?
        //        Bukkit.getConsoleSender() : null;
        //loadFile(sender, filePath);
        LuaValue chunk = table.loadfile(DATA_PATH + filePath + FILE_EXTENSION);
        chunk.call();
        MessageUtil.debugLog(lockout, "Tasks from " + filePath + " loaded.");
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
