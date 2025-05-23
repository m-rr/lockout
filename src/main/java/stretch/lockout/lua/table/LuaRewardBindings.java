package stretch.lockout.lua.table;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.lua.LuaPlayerConsumer;
import stretch.lockout.lua.LuaPotionEffect;
import stretch.lockout.lua.Compatability;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.task.TaskInvisible;
import stretch.lockout.util.MessageUtil;
import stretch.lockout.reward.*;

import java.util.*;

public class LuaRewardBindings implements LuaTableBinding {

    private final LockoutContext lockout;

    public LuaRewardBindings(final LockoutContext lockout) {
        this.lockout = lockout;
    }

    @Override
    public void injectBindings(LuaTable table) {
        //item(material, amount, rewardType, description, {optional} enchantment table, {optional} itemMetaConsumer)
        table.set("item", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Material material = (Material) CoerceLuaToJava.coerce(args.arg(1), Material.class);
                int amount = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(args.arg(3), RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(4), String.class);

                ItemStack item = new ItemStack(material, amount);

                if (args.arg(5).istable()) {
                    LuaTable enchantmentTable = args.arg(5).checktable();
                    Map<Enchantment, Integer> enchantments = new HashMap<>();
                    for (var keyName : enchantmentTable.keys()) {
                        String key = Compatability.ENCHANT.keySet().contains(keyName.tojstring().toLowerCase()) ?
                            Compatability.ENCHANT.get(keyName.tojstring().toLowerCase())
                            : keyName.tojstring().toLowerCase();

                        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key.toLowerCase()));

                        int value = (int) CoerceLuaToJava.coerce(enchantmentTable.get(keyName.tojstring().toLowerCase()), int.class);
                        enchantments.put(enchantment, value);
                    }
                    item.addUnsafeEnchantments(enchantments);
                }

                if (args.arg(6).isfunction()) {
                    LuaFunction itemMetaConsumer = args.arg(6).checkfunction();
                    itemMetaConsumer.call(CoerceJavaToLua.coerce(item));
                }
                return CoerceJavaToLua.coerce(new RewardItem(item, rewardType, description));
            }
        });

        //potion(potioneffectType, amplifier, rewardType, description, {optional} timeTicks)
        table.set("potion", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaPotionEffect effect = (LuaPotionEffect) CoerceLuaToJava.coerce(args.arg(1), LuaPotionEffect.class);
                PotionEffectType potionEffectType = effect.getEffect();

                int amplifier = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(args.arg(3), RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(4), String.class);

                int potionTime = args.arg(5).isint()
                        ? args.arg(5).checkint()
                        : (int) lockout.settings().getRewardPotionTicks();

                PotionEffect potionEffect = new PotionEffect(potionEffectType, potionTime, amplifier - 1);
                return CoerceJavaToLua.coerce(new RewardPotion(potionEffect, rewardType, description));
            }
        });

        //action(rewardType, description, consumer)
        table.set("action", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(luaValue, RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(luaValue1, String.class);

                return CoerceJavaToLua.coerce(new RewardAction(new LuaPlayerConsumer(luaValue2), rewardType, description));
            }
        });

        //rewardTask(rewardType, description, task)
        table.set("rewardTask", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(luaValue, RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(luaValue1, String.class);
                TaskComponent taskComponent = (TaskComponent) CoerceLuaToJava.coerce(luaValue2, TaskComponent.class);

                TaskInvisible taskInvisible = new TaskInvisible(taskComponent);
                lockout.getCurrentTaskCollection().addTask(taskInvisible);

                return CoerceJavaToLua.coerce(new RewardTask(taskInvisible, rewardType, description));
            }
        });

        //rewardChance(description, rewardTable)
        table.set("rewardChance", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                String description = (String) CoerceLuaToJava.coerce(luaValue, String.class);
                LuaTable rewardTable = luaValue1.checktable();
                List<RewardChance.WeightedReward> weightedRewards = Arrays.stream(rewardTable.keys())
                        .map(r -> new RewardChance.WeightedReward(
                                (RewardComponent) CoerceLuaToJava.coerce(r, RewardComponent.class), rewardTable.get(r).checkint()))
                        .toList();

                return CoerceJavaToLua.coerce(new RewardChance(description, weightedRewards));
            }
        });

        //rewards(reward..rewards)
        table.set("rewards", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                List<RewardComponent> rewards = new ArrayList<>();
                for (int i = 1; i <= args.narg(); i++) {
                    RewardComponent reward = (RewardComponent) CoerceLuaToJava.coerce(args.arg(i), RewardComponent.class);
                    rewards.add(reward);
                }

                return CoerceJavaToLua.coerce(new RewardComposite(rewards));
            }
        });

    }
}
