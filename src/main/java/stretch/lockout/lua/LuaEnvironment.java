package stretch.lockout.lua;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;
import stretch.lockout.game.GameState;
import stretch.lockout.game.LockoutWrapper;
import stretch.lockout.game.RaceGameContext;
import stretch.lockout.reward.RewardType;
import stretch.lockout.task.TaskComponent;
import stretch.lockout.util.types.BiomeTypes;
import stretch.lockout.util.types.MaterialTypes;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class LuaEnvironment {
    private Globals table;
    private final RaceGameContext lockout;
    private final Random random = new Random();

    public LuaEnvironment(final RaceGameContext lockout) {
        this.lockout = lockout;
        this.table = JsePlatform.standardGlobals();
        initTable();
    }

    public void loadFile(CommandSender sender, String filePath) {
        LuaValue chunk = table.loadfile(filePath);
        chunk.call();
        sender.sendMessage("Lua file " + filePath + " loaded.");
    }

    public void loadFile(String filePath) {
        loadFile(Bukkit.getConsoleSender(), filePath);
    }

    private void initTable() {
        table.set("Bukkit", CoerceJavaToLua.coerce(Bukkit.class));
        table.set("Material", CoerceJavaToLua.coerce(Material.class));
        table.set("Biome", CoerceJavaToLua.coerce(Biome.class));
        table.set("BlockFace", CoerceJavaToLua.coerce(BlockFace.class));

        table.set("Lockout", new LockoutWrapper(lockout));
        table.set("RewardType", CoerceJavaToLua.coerce(RewardType.class));
        table.set("GameState", CoerceJavaToLua.coerce(GameState.class));
        table.set("BiomeType", CoerceJavaToLua.coerce(BiomeTypes.class));
        table.set("MaterialType", CoerceJavaToLua.coerce(MaterialTypes.class));

        table.set("addTask", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(arg, TaskComponent.class);
                lockout.getTaskManager().addTask(task);
                return CoerceJavaToLua.coerce(task);
            }
        });

        table.set("oneOf", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int size = 0;
                LuaValue curr = args.arg1();
                while (!curr.isnil()) {
                    size++;
                    curr = args.arg(size + 1);
                }

                return CoerceJavaToLua.coerce(args.arg(random.nextInt(1, size + 1)));
            }
        });

        initPredicates();
        initTaskBuilders();
    }

    public void initTaskBuilders() {
        // place(material, value, description, guiMaterial)
        table.set("place", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(TaskBuilder.createTaskMaterial(BlockPlaceEvent.class, args));
            }
        });

        // destroy(entitytype, value, description, guiMaterial)
        table.set("destroy", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(TaskBuilder.createTaskMaterial(BlockBreakEvent.class, args));
            }
        });
    }

    private void initPredicates() {
        // inMainHand(entity, material)
        table.set("inMainHand", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                return CoerceJavaToLua.coerce(entity.getEquipment().getItemInMainHand().getType() == material);
            }
        });

        // inBiome(entity, biome)
        table.set("inBiome", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Biome biome = (Biome) CoerceLuaToJava.coerce(luaValue1, Biome.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getBlock().getBiome() == biome);
            }
        });

        // inBiomeType(entity, biomeType)
        table.set("inBiomeType", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Set<Biome> biomes = (ImmutableSet<Biome>) CoerceLuaToJava.coerce(luaValue1, ImmutableSet.class);
                return CoerceJavaToLua.coerce(biomes.contains(entity.getLocation().getBlock().getBiome()));
            }
        });

        // onBlock(entity, blockMaterial)
        table.set("onBlock", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == material);
            }
        });

        // hasMaxHealth(entity)
        table.set("hasMaxHealth", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() == entity.getHealth());
            }
        });

        // wearingArmor(entity, armorMaterial)
        table.set("wearingArmor", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                Material material = (Material) CoerceLuaToJava.coerce(luaValue1, Material.class);
                Set<Material> armor = Arrays.stream(entity.getEquipment().getArmorContents()).map(ItemStack::getType).collect(Collectors.toSet());
                return CoerceJavaToLua.coerce(armor.contains(material));
            }
        });

        // aboveY(entity, yValue)
        table.set("aboveY", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                double y = (double) CoerceLuaToJava.coerce(luaValue1, double.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getY() >= y);
            }
        });

        // hasPotionEffect(entity, potionEffectType)
        table.set("hasPotionEffect", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                PotionEffectType potionEffectType = (PotionEffectType) CoerceLuaToJava.coerce(luaValue1, PotionEffectType.class);
                return CoerceJavaToLua.coerce(entity.getPotionEffect(potionEffectType) != null);
            }
        });

        // onFire(entity)
        table.set("onFire", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.getFireTicks() > 0);
            }
        });

        // inWater(entity)
        table.set("inWater", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.isInWater());
            }
        });

        // isDay(entity)
        table.set("isDay", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                final long NIGHT_START = 13000;
                return CoerceJavaToLua.coerce(entity.getWorld().getTime() < NIGHT_START);
            }
        });

        // onSurface(entity)
        table.set("onSurface", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                return CoerceJavaToLua.coerce(entity.getLocation().getBlock().getLightFromSky() == 15);
            }
        });

        // isEntity(entity, targetEntityType)
        table.set("isEntity", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                LivingEntity entity = (LivingEntity) CoerceLuaToJava.coerce(luaValue, LivingEntity.class);
                EntityType targetEntityType = (EntityType) CoerceLuaToJava.coerce(luaValue1, EntityType.class);
                return CoerceJavaToLua.coerce(entity.getType() == targetEntityType);
            }
        });

    }

}
