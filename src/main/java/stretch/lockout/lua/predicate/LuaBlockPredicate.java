package stretch.lockout.lua.predicate;

import org.bukkit.block.Block;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.function.Predicate;

public class LuaBlockPredicate implements Predicate<Block> {
    private final LuaFunction predicate;

    public LuaBlockPredicate(LuaValue predicate) {
        this.predicate = predicate.checkfunction();
    }

    @Override
    public boolean test(Block block) {
        LuaValue pred = CoerceJavaToLua.coerce(block);
        return (boolean) CoerceLuaToJava.coerce(predicate.call(pred), boolean.class);
    }
}
