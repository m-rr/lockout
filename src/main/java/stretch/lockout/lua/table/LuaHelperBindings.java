package stretch.lockout.lua.table;

import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.game.LockoutGameRule;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.lua.LuaTaskBuilder;
import stretch.lockout.task.*;

import java.time.Duration;
import java.util.*;

public class LuaHelperBindings implements LuaTableBinding {
    private final Random random = new Random();
    private final LockoutContext lockout;
    public LuaHelperBindings(final LockoutContext lockout) {
        this.lockout = lockout;
    }
    @Override
    public void injectBindings(LuaTable table) {
        table.set("addTask", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(luaValue, TaskComponent.class);
                lockout.getMainTasks().addTask(task);
                return CoerceJavaToLua.coerce(task);
            }
        });

        table.set("addTieBreaker", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(luaValue, TaskComponent.class);
                lockout.getTieBreaker().addTask(task);
                return CoerceJavaToLua.coerce(task);
            }
        });

        //createItem(material, amount)
        table.set("createItem", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Material material = (Material) CoerceLuaToJava.coerce(luaValue, Material.class);
                int amount = (int) CoerceLuaToJava.coerce(luaValue1, int.class);
                return CoerceJavaToLua.coerce(new ItemStack(material, amount));
            }
        });

        //copyItem(itemStack)
        table.set("copyItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                ItemStack itemStack = (ItemStack) CoerceLuaToJava.coerce(luaValue, ItemStack.class);
                return itemStack == null ?
                        null : CoerceJavaToLua.coerce(new ItemStack(itemStack));
            }
        });

        table.set("taskCount", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(lockout.getCurrentTaskCollection().getTaskCount());
            }
        });

        table.set("setMaxScore", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                int score = (int) CoerceLuaToJava.coerce(luaValue, int.class);
                lockout.settings().setMaxScore(score);
                return null;
            }
        });

        table.set("setTimer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                long minutes = (long) CoerceLuaToJava.coerce(luaValue, long.class);
                if (lockout.settings().hasRule(LockoutGameRule.TIMER)) {
                    lockout.getUiManager().getTimer().setTime(Duration.ofMinutes(minutes));
                }
                return null;
            }
        });

        table.set("trackedPlayer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                Player player = (Player) CoerceLuaToJava.coerce(luaValue, Player.class);
                Player trackedPlayer = Optional.ofNullable(lockout.getPlayerTracker().getTrackedPlayer(player))
                        .orElse(player);
                return CoerceJavaToLua.coerce(trackedPlayer);
            }
        });

        table.set("containsBiomeType", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Set<Biome> biomes = (Set<Biome>) CoerceLuaToJava.coerce(luaValue, Set.class);
                Set<Biome> biomeType = (Set<Biome>) CoerceLuaToJava.coerce(luaValue1, Set.class);
                Set<Biome> result = new HashSet<>(biomeType);
                result.retainAll(biomes);
                return CoerceJavaToLua.coerce(!result.isEmpty());
            }
        });

        table.set("getBiomes", new TwoArgFunction() {
            private Set<Biome> findBiomes(final Location start, final Location velocity, final int dist) {
                Set<Biome> result = new HashSet<>();
                Location curr = start.clone();
                for (int i = 0; i < dist; i++) {
                    result.add(curr.getBlock().getBiome());
                    curr = curr.add(velocity);
                }
                return result;
            }

            // getBiomes(start, distance)
            private Set<Biome> getBiomes(final Location start, final int dist) {
                final Location posX = new Location(start.getWorld(), 15D, 0D, 0D);
                final Location negX = new Location(start.getWorld(), -15D, 0D, 0D);
                final Location posZ = new Location(start.getWorld(), 0D, 0D, 15D);
                final Location negZ = new Location(start.getWorld(), 0D, 0D, -15D);

                var biomes = List.of(
                        findBiomes(start, posX, dist),
                        findBiomes(start, negX, dist),
                        findBiomes(start, posZ, dist),
                        findBiomes(start, negZ, dist),
                        findBiomes(start, posX.add(posZ), dist),
                        findBiomes(start, posX.add(negZ), dist),
                        findBiomes(start, negX.add(posZ), dist),
                        findBiomes(start, negX.add(negZ), dist));

                return biomes.stream()
                        .reduce(Sets::union)
                        .get();
            }
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Location start = (Location) CoerceLuaToJava.coerce(luaValue, Location.class);
                int distance = (int) CoerceLuaToJava.coerce(luaValue1, int.class);
                return CoerceJavaToLua.coerce(getBiomes(start, distance));
            }
        });

        // oneOf(task..tasks)
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

        // anyOf(guiMaterial, value, description, task..tasks)
        table.set("anyOf", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createComposite(args, TaskORComposite.class));
            }
        });

        // allOf(guiMaterial, value, description, task..tasks)
        table.set("allOf", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createComposite(args, TaskANDComposite.class));
            }
        });

        // sequential(guiMaterial, value, description, task..tasks)
        table.set("sequential", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createComposite(args, TaskTHENComposite.class));
            }
        });

        table.set("doTimes", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                int times = (int) CoerceLuaToJava.coerce(luaValue, int.class);
                TimeCompletableTask task = (TimeCompletableTask) CoerceLuaToJava.coerce(luaValue1, TimeCompletableTask.class);
                return CoerceJavaToLua.coerce(new TaskRepeat(task, times));
            }
        });

        // createLoc(world, x, y, z)
        table.set("createLoc", new VarArgFunction() {
               @Override
               public LuaValue invoke(Varargs args) {
                   World world = (World) CoerceLuaToJava.coerce(args.arg(1), World.class);
                   double x = (double) CoerceLuaToJava.coerce(args.arg(2), double.class);
                   double y = (double) CoerceLuaToJava.coerce(args.arg(3), double.class);
                   double z = (double) CoerceLuaToJava.coerce(args.arg(4), double.class);

                   return CoerceJavaToLua.coerce(new Location(world, x, y, z));
               }
            });
    }
}
