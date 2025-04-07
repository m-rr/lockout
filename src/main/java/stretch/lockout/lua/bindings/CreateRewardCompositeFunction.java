package stretch.lockout.lua.bindings;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.reward.pattern.RewardComposite;
import stretch.lockout.util.LockoutLogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Luaj Function binding to allow creating RewardComposite objects from Lua.
 * Designed to be called from the 'rewards.combine(rewards_table)' Lua helper.
 * Expects exactly one argument from Lua:
 * 1. rewards_table (Lua table/list where each element is a RewardComponent userdata)
 */
public class CreateRewardCompositeFunction extends VarArgFunction {

    public CreateRewardCompositeFunction() {

    }

    @Override
    public Varargs invoke(Varargs args) {

        // Expect exactly one argument: the table passed by rewards.combine()
        if (args.narg() != 1) {
            throw new LuaError("RewardComposite binding expects exactly one argument (a table of rewards) but received " + args.narg());
        }
        // Use checktable() which automatically throws LuaError if arg(1) is not a table
        LuaTable rewardsTable = args.checktable(1);

        int tableLength = rewardsTable.rawlen(); // Get sequence length safely
        List<RewardComponent> rewardsList = new ArrayList<>(tableLength);

        for (int i = 1; i <= tableLength; i++) {
            LuaValue luaReward = rewardsTable.get(i);
            RewardComponent rewardComponent;

            if (luaReward.isnil()) {
                throw new LuaError("Reward list contains a nil value at index " + i);
            }

            try {
                // Attempt coercion to the interface type
                Object coerced = CoerceLuaToJava.coerce(luaReward, RewardComponent.class);

                // Check if coercion was successful and is the correct type
                if (coerced instanceof RewardComponent) {
                    rewardComponent = (RewardComponent) coerced;
                } else {
                    // Coercion returned null or wrong type
                    throw new LuaError("Element at index " + i + " is not a valid RewardComponent (Type: " + luaReward.typename() + ")");
                }
            } catch (LuaError e) {
                // Re-throw coercion errors with more context
                throw new LuaError("Error coercing element at index " + i + " to RewardComponent: " + e.getMessage());
            } catch (Exception e) {
                // Catch unexpected Java errors during coercion
                throw new LuaError("Unexpected Java error processing element at index " + i + ": " + e.getMessage());
            }

            // 4. Add Validated Component to List
            // We already threw an error if rewardComponent is null above
            rewardsList.add(rewardComponent);
        }

        // Optional: Check if the list ended up empty (e.g., input was {})
        if (rewardsList.isEmpty() && tableLength > 0) {
            // This case implies all elements failed validation somehow, which shouldn't happen if errors are thrown above.
            // Add a warning just in case.
            LockoutLogger.warning("RewardComposite created with empty list despite non-empty input table for: " + args.tojstring());
        } else if (rewardsList.isEmpty()) {
            LockoutLogger.warning("RewardComposite created with empty reward list as input was empty.");
        }


        RewardComposite composite = new RewardComposite(rewardsList);

        return CoerceJavaToLua.coerce(composite);
    }
}
