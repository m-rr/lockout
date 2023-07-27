package stretch.lockout.lua;

import org.bukkit.entity.Player;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.function.Consumer;

public class LuaPlayerConsumer implements Consumer<Player> {
    private final LuaFunction consumer;
    public LuaPlayerConsumer(LuaValue consumer) {
        this.consumer = consumer.checkfunction();
    }
    @Override
    public void accept(Player player) {
        LuaValue luaPlayer = CoerceJavaToLua.coerce(player);
        consumer.call(luaPlayer);
    }
}
