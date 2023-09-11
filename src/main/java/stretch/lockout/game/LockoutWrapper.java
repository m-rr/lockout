package stretch.lockout.game;

import org.bukkit.Bukkit;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

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
        this.set("setMaxScore", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                int score = (int) CoerceLuaToJava.coerce(luaValue, int.class);
                lockout.setMaxScore(score);
                return CoerceJavaToLua.coerce(score);
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
