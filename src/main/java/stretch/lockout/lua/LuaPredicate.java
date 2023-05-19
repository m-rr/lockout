package stretch.lockout.lua;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.function.Predicate;

public class LuaPredicate implements Predicate<HumanEntity> {
    private final LuaFunction predicate;
    public LuaPredicate(LuaValue predicate) {
        this.predicate = predicate.checkfunction();
    }

    @Override
    public boolean test(HumanEntity player) {
        LuaValue p = CoerceJavaToLua.coerce(player);
        return (boolean) CoerceLuaToJava.coerce(predicate.call(p), boolean.class);
    }
}
