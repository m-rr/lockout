package stretch.lockout.lua.table;

import org.luaj.vm2.LuaTable;

public interface LuaTableBinding {
    void injectBindings(LuaTable table);
}
