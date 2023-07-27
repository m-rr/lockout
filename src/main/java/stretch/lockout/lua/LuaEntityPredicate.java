package stretch.lockout.lua;

import org.bukkit.entity.Entity;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.function.Predicate;

public class LuaEntityPredicate implements Predicate<Entity> {
    private final LuaFunction predicate;
    public LuaEntityPredicate(LuaValue predicate) {
        this.predicate = predicate.checkfunction();
    }
    @Override
    public boolean test(Entity entity) {
        LuaValue pred = CoerceJavaToLua.coerce(entity);
        return (boolean) CoerceLuaToJava.coerce(predicate.call(pred), boolean.class);
    }
}
