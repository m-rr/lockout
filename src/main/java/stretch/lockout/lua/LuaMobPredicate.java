package stretch.lockout.lua;

import org.bukkit.entity.Mob;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.function.Predicate;

public class LuaMobPredicate implements Predicate<Mob> {
    private final LuaFunction predicate;
    public LuaMobPredicate(LuaValue predicate) {
        this.predicate = predicate.checkfunction();
    }
    @Override
    public boolean test(Mob mob) {
        LuaValue pred = CoerceJavaToLua.coerce(mob);
        return (boolean) CoerceLuaToJava.coerce(predicate.call(pred), boolean.class);
    }
}
