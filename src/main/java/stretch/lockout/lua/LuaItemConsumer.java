package stretch.lockout.lua;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.function.Consumer;

public class LuaItemConsumer implements Consumer<ItemStack> {
    private final LuaFunction consumer;
    public LuaItemConsumer(LuaValue consumer) {
        this.consumer = consumer.checkfunction();
    }
    @Override
    public void accept(ItemStack itemStack) {
        LuaValue item = CoerceJavaToLua.coerce(itemStack);
        consumer.call(item);
    }
}
