package stretch.lockout.lua.bindings; // Example package

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.reward.pattern.RewardChance;
import stretch.lockout.util.LockoutLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Luaj Function binding to allow creating RewardChance objects from Lua.
 * Expects two arguments from Lua:
 * 1. description (string)
 * 2. weighted_rewards_table (Lua table/list where each element is a table
 * like { reward = RewardComponentUserdata, weight = integer })
 */
public class CreateRewardChanceFunction extends VarArgFunction {

    public CreateRewardChanceFunction() {

    }

    @Override
    public Varargs invoke(Varargs args) {
        // 1. Validate Arguments
        if (args.narg() != 2) {
            throw new LuaError("RewardChance constructor requires exactly 2 arguments (description, weighted_rewards_table)");
        }

        String description = args.checkjstring(1); // Arg 1: description string
        LuaTable weightedTable = args.checktable(2); // Arg 2: weighted rewards table

        // 2. Parse the Lua Table into a Java List
        List<RewardChance.WeightedReward> weightedList = new ArrayList<>();
        int tableLength = weightedTable.rawlen(); // Get sequence length

        for (int i = 1; i <= tableLength; i++) {
            LuaValue itemValue = weightedTable.get(i);
            if (!itemValue.istable()) {
                throw new LuaError("Item at index " + i + " in weighted_rewards_table must be a table {reward=..., weight=...}");
            }
            LuaTable itemTable = (LuaTable) itemValue;

            LuaValue rewardValue = itemTable.get("reward");
            LuaValue weightValue = itemTable.get("weight");

            // Validate inner table structure
            if (rewardValue.isnil() || !weightValue.isint() || weightValue.toint() <= 0) {
                throw new LuaError("Item at index " + i + " requires non-nil 'reward' (RewardComponent) and positive integer 'weight'. Found reward=" + rewardValue.typename() + ", weight=" + weightValue.tojstring());
            }

            // Coerce Lua values to Java types
            try {
                // Use the interface RewardComponent for coercion
                RewardComponent rewardComponent = (RewardComponent) CoerceLuaToJava.coerce(rewardValue, RewardComponent.class);
                int weight = weightValue.toint(); // Already checked isint and > 0

                if (rewardComponent == null) {
                    // Coercion might return null if type is wrong, though checkuserdata might be better
                    throw new LuaError("Item at index " + i + ": 'reward' value could not be coerced to RewardComponent.");
                }

                // Create the Java WeightedReward object (ensure WeightedReward is accessible)
                RewardChance.WeightedReward weightedReward = new RewardChance.WeightedReward(rewardComponent, weight);
                weightedList.add(weightedReward);

            } catch (LuaError e) {
                // Re-throw coercion errors with more context
                throw new LuaError("Error processing item at index " + i + " in weighted_rewards_table: " + e.getMessage());
            } catch (Exception e) {
                // Catch other potential Java exceptions during coercion/creation
                throw new LuaError("Unexpected Java error processing item at index " + i + ": " + e.getMessage());
            }
        }

        // Check if any rewards were actually parsed
        if (weightedList.isEmpty()) {
            // This could happen if the input table was technically a table but empty [{}] etc.
            //if (logger != null) logger.warning("RewardChance created with empty weighted reward list for description: " + description);
            LockoutLogger.warning("RewardChance created with empty weighted reward list for description: " + description);
            // Depending on requirements, you might throw an error or allow empty RewardChance
            // throw new LuaError("Weighted reward list cannot be empty.");
        }


        // 3. Create the Java RewardChance Object
        RewardChance rewardChance = new RewardChance(description, weightedList);

        // 4. Return the Java object coerced to Lua userdata
        return CoerceJavaToLua.coerce(rewardChance);
    }
}