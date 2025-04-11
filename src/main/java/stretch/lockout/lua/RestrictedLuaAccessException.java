package stretch.lockout.lua;

import org.luaj.vm2.LuaError;

public class RestrictedLuaAccessException extends LuaError {
    public RestrictedLuaAccessException(String message) {
        super(message);
    }
}
