package stretch.lockout.game;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LockoutWrapper extends LuaTable {
    private final RaceGameContext lockout;
    public LockoutWrapper(RaceGameContext lockout) {
        this.lockout = lockout;
        this.set("getMaxScore", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(lockout.getMaxScore());
            }
        });
        this.set("getGameState", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(lockout.getGameState());
            }
        });
        this.set("getWorld", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(lockout.getGameWorld());
            }
        });
        this.set("lockout", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(lockout);
            }
        });
    }
}
