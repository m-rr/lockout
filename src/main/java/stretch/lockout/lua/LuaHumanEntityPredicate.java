package stretch.lockout.lua;

import org.bukkit.entity.HumanEntity;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.function.Predicate;

public class LuaHumanEntityPredicate implements Predicate<HumanEntity> {
    private final LuaFunction predicate;
    public LuaHumanEntityPredicate(LuaValue predicate) {
        this.predicate = predicate.checkfunction();
    }

    @Override
    public boolean test(HumanEntity player) {
        LuaValue pred = CoerceJavaToLua.coerce(player);
        return (boolean) CoerceLuaToJava.coerce(predicate.call(pred), boolean.class);
    }
}
