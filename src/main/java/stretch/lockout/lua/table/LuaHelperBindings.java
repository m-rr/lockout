package stretch.lockout.lua.table;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.LuaTaskBuilder;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.task.composite.RepeatingTask;
import stretch.lockout.task.composite.TaskChoice;
import stretch.lockout.task.composite.TaskSequence;
import stretch.lockout.task.composite.TaskSet;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.tracker.PlayerTracker;
import stretch.lockout.ui.bar.LockoutTimer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LuaHelperBindings implements LuaTableBinding {
    private final Random random = new Random();
    private final LockoutSettings settings;
    private final TaskCollection tasks;
    private final LockoutTimer timer;
    private final PlayerTracker tracker;
    private final Plugin plugin;
    public LuaHelperBindings(final Plugin plugin, final LockoutSettings settings, final TaskCollection tasks, final LockoutTimer timer, final PlayerTracker playerTracker) {
        this.settings = settings;
        this.tasks = tasks;
        this.timer = timer;
        this.tracker = playerTracker;
        this.plugin = plugin;
    }
    @Override
    public void injectBindings(LuaTable table) {
        table.set("_addTask", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(luaValue, TaskComponent.class);
                tasks.addTask(task);
                return CoerceJavaToLua.coerce(task);
            }
        });

        table.set("_addTieBreaker", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(luaValue, TaskComponent.class);
                //lockout.getTieBreaker().addTask(task);
                throw new UnsupportedOperationException("_addTieBreaker is not supported");
                //return CoerceJavaToLua.coerce(task);
            }
        });

        //createItem(material, amount)
        table.set("_createItem", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Material material = (Material) CoerceLuaToJava.coerce(luaValue, Material.class);
                int amount = (int) CoerceLuaToJava.coerce(luaValue1, int.class);
                return CoerceJavaToLua.coerce(new ItemStack(material, amount));
            }
        });

        //copyItem(itemStack)
        table.set("_copyItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                ItemStack itemStack = (ItemStack) CoerceLuaToJava.coerce(luaValue, ItemStack.class);
                return itemStack == null ?
                        null : CoerceJavaToLua.coerce(new ItemStack(itemStack));
            }
        });

        table.set("_taskCount", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(tasks.getTaskCount());
            }
        });

        table.set("_setMaxScore", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                int score = (int) CoerceLuaToJava.coerce(luaValue, int.class);
                settings.setMaxScore(score);
                return null;
            }
        });

        table.set("_setTimer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                long minutes = (long) CoerceLuaToJava.coerce(luaValue, long.class);
                if (settings.hasRule(LockoutGameRule.TIMER)) {
                    timer.setTime(Duration.ofMinutes(minutes));
                }
                return null;
            }
        });

        table.set("_trackedPlayer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                Player player = (Player) CoerceLuaToJava.coerce(luaValue, Player.class);
                Player trackedPlayer = Optional.ofNullable(tracker.getTrackedPlayer(player))
                        .orElse(player);
                return CoerceJavaToLua.coerce(trackedPlayer);
            }
        });

        table.set("_containsBiomeType", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Set<Biome> biomes = (Set<Biome>) CoerceLuaToJava.coerce(luaValue, Set.class);
                Set<Biome> biomeType = (Set<Biome>) CoerceLuaToJava.coerce(luaValue1, Set.class);
                Set<Biome> result = new HashSet<>(biomeType);
                result.retainAll(biomes);
                return CoerceJavaToLua.coerce(!result.isEmpty());
            }
        });

        table.set("_getBiomes", new TwoArgFunction() {
            private void findBiomes(final Location start,
                                          final Location velocity,
                                          final int dist,
                                    ConcurrentHashMap<Biome, Integer> biomes) {
                Supplier<Biome> biomeSupplier = new Supplier<>() {
                    final Location curr = start.clone();

                    @Override
                    public Biome get() {
                        curr.add(velocity);
                        return curr.getBlock().getBiome();
                    }
                };

                for (int i = 0; i < dist; i++) {
                    Biome biome = biomeSupplier.get();
                    biomes.compute(biome, (k, v) -> v == null ? 1 : v + 1);
                }
            }

            private Map<Biome, Integer> getBiomes(final Location start, final int dist) {
                final Location posX = new Location(start.getWorld(), 15D, 0D, 0D);
                final Location negX = new Location(start.getWorld(), -15D, 0D, 0D);
                final Location posZ = new Location(start.getWorld(), 0D, 0D, 15D);
                final Location negZ = new Location(start.getWorld(), 0D, 0D, -15D);

                var regionScheduler = Bukkit.getRegionScheduler();
                final ConcurrentHashMap<Biome, Integer> biomes = new ConcurrentHashMap<>();
                // check for biomes in a snowflake shape
                regionScheduler.run(plugin, start, task -> findBiomes(start, posX, dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, negX, dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, posZ, dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, negZ, dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, posX.add(posZ), dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, posX.add(negZ), dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, negX.add(posZ), dist, biomes));
                regionScheduler.run(plugin, start, task -> findBiomes(start, negX.add(negZ), dist, biomes));

                return biomes;
            }
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                Location start = (Location) CoerceLuaToJava.coerce(luaValue, Location.class);
                int distance = (int) CoerceLuaToJava.coerce(luaValue1, int.class);
                var biomes = getBiomes(start, distance);
                return CoerceJavaToLua.coerce(biomes);
            }
        });

        // oneOf(task..tasks)
        table.set("_oneOf", new VarArgFunction() {
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
        table.set("_anyOf", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createComposite(args, TaskChoice.class));
            }
        });

        // allOf(guiMaterial, value, description, task..tasks)
        table.set("_allOf", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createComposite(args, TaskSet.class));
            }
        });

        // sequential(guiMaterial, value, description, task..tasks)
        table.set("_sequential", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return CoerceJavaToLua.coerce(LuaTaskBuilder.createComposite(args, TaskSequence.class));
            }
        });

        table.set("_doTimes", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
                int times = (int) CoerceLuaToJava.coerce(luaValue, int.class);
                TimeCompletableTask task = (TimeCompletableTask) CoerceLuaToJava.coerce(luaValue1, TimeCompletableTask.class);
                return CoerceJavaToLua.coerce(new RepeatingTask(task, times));
            }
        });

        // createLoc(world, x, y, z)
        table.set("_createLoc", new VarArgFunction() {
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
