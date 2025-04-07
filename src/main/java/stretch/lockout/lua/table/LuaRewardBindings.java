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
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.consumer.LuaPlayerConsumer;
import stretch.lockout.lua.LuaPotionEffect;
import stretch.lockout.lua.Compatability;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.reward.impl.RewardAction;
import stretch.lockout.reward.impl.RewardItem;
import stretch.lockout.reward.impl.RewardPotion;
import stretch.lockout.reward.impl.RewardTask;
import stretch.lockout.reward.pattern.RewardChance;
import stretch.lockout.reward.pattern.RewardComposite;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.HiddenTask;
import stretch.lockout.task.manager.TaskCollection;

import java.util.*;

public class LuaRewardBindings implements LuaTableBinding {

    private final TaskCollection tasks;
    private final LockoutSettings settings;
    public LuaRewardBindings(final LockoutSettings settings, final TaskCollection tasks) {
        this.tasks = tasks;
        this.settings = settings;
    }

    @Override
    public void injectBindings(LuaTable table) {
        //item(material, amount, rewardType, description, {optional} enchantment table, {optional} itemMetaConsumer)
        table.set("_item", new VarArgFunction() {
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
                        //String key = Compatability.ENCHANT.keySet().contains(keyName.tojstring().toLowerCase()) ?
                            //Compatability.ENCHANT.get(keyName.tojstring().toLowerCase())
                            //: keyName.tojstring().toLowerCase();

                        //Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key.toLowerCase()));
                        Enchantment enchantment = (Enchantment) CoerceLuaToJava.coerce(keyName, Enchantment.class);

                        int value = (int) CoerceLuaToJava.coerce(enchantmentTable.get(keyName), int.class);
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
        table.set("_potion", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaPotionEffect effect = (LuaPotionEffect) CoerceLuaToJava.coerce(args.arg(1), LuaPotionEffect.class);
                PotionEffectType potionEffectType = effect.getEffect();

                int amplifier = (int) CoerceLuaToJava.coerce(args.arg(2), int.class);
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(args.arg(3), RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(args.arg(4), String.class);

                int potionTime = args.arg(5).isint()
                        ? args.arg(5).checkint()
                        : (int) settings.getRewardPotionTicks();
                
                PotionEffect potionEffect = new PotionEffect(potionEffectType, potionTime, amplifier - 1);
                return CoerceJavaToLua.coerce(new RewardPotion(potionEffect, rewardType, description));
            }
        });

        //action(rewardType, description, consumer)
        table.set("_action", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(luaValue, RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(luaValue1, String.class);

                return CoerceJavaToLua.coerce(new RewardAction(new LuaPlayerConsumer(luaValue2), rewardType, description));
            }
        });

        //rewardTask(rewardType, description, task)
        table.set("_rewardTask", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1, LuaValue luaValue2) {
                RewardType rewardType = (RewardType) CoerceLuaToJava.coerce(luaValue, RewardType.class);
                String description = (String) CoerceLuaToJava.coerce(luaValue1, String.class);
                TaskComponent taskComponent = (TaskComponent) CoerceLuaToJava.coerce(luaValue2, TaskComponent.class);

                HiddenTask taskInvisible = new HiddenTask(taskComponent);
                tasks.addTask(taskInvisible);

                return CoerceJavaToLua.coerce(new RewardTask(taskInvisible, rewardType, description));
            }
        });

        //rewardChance(description, rewardTable)
        table.set("_rewardChance", new TwoArgFunction() {
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
        table.set("_rewards", new VarArgFunction() {
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
