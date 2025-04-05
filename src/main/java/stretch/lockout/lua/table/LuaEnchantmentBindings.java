package stretch.lockout.lua.table;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import stretch.lockout.lua.provider.EnchantmentProvider;

public class LuaEnchantmentBindings implements LuaTableBinding{

    @Override
    public void injectBindings(LuaTable table) {
        var enchantments = EnchantmentProvider.getINSTANCE().values();
        for (var enchantment : enchantments) {
            table.set(enchantment.getKey().value(), CoerceJavaToLua.coerce(enchantment));
        }
    }
}
