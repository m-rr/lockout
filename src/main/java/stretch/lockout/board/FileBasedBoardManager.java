package stretch.lockout.board;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.manager.TaskManager;
import stretch.lockout.util.LockoutLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link BoardManager} which operates on the local filesystem.
 * Responsible for the {@link LuaEnvironment} lifecycle associated with a loaded board.
 *
 * @author m-rr
 * @version @projectVersion@
 * @see LuaEnvironment
 * @see BoardInfo
 * @see BoardManager
 * @see InvalidBoardPropertiesException
 * @since 2.5.1
 * */
public class FileBasedBoardManager implements BoardManager {
    private final Path BOARD_PATH = Paths.get("plugins/Lockout/");
    LuaEnvironment luaEnvironment;
    private final LockoutSettings settings;
    private final Plugin plugin;
    private final List<BoardInfo> boards = new ArrayList<>();
    private boolean hasBoardDefinition = false;
    private BoardDefinition boardDefinition = null;
    private final TaskManager taskManager;

    public FileBasedBoardManager(@NonNull Plugin plugin, @NonNull LockoutSettings settings, @NonNull TaskManager taskManager) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.settings = Objects.requireNonNull(settings, "Settings cannot be null");
        this.taskManager = Objects.requireNonNull(taskManager, "TaskManager cannot be null");
        this.luaEnvironment = new LuaEnvironment(plugin, settings, true);
    }

    /**
     * @author m-rr
     * @return All board metadata found on search path.
     * @since 2.5.1
     * */
    public List<BoardInfo> getBoards() {
        return boards;
    }

    /**
     * Finds the first board in object memory where info.name == name.
     *
     * @author m-rr
     * @param name info.name key in board.properties used as the main identifier.
     * @return An {@link Optional} BoardInfo specified by name
     * @since 2.5.1
     * */
    public Optional<BoardInfo> getBoard(final String name) {
        return getBoards().stream()
                .filter(board -> board.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Attempts to load lua files using a {@link LuaEnvironment}.
     * Only searches for boards which were found by this object
     * using ({@code RegisterBoardsAsync}). If the board is found,
     * then all keys in the board.properties file are injected into
     * the {@link LuaEnvironment}, allowing boards to have parameters
     * which can be passed to them as a config file.The relative path
     * of the {@link LuaEnvironment} is set to the root directory of
     * the board.properties file, disallowing imports from other board modules.
     *
     * @author m-rr
     * @param name of board specified by info.name key in board.properties.
     * @throws NoSuchElementException if name does not match a board.
     * @since 2.5.1
     * */
    public void loadBoard(final String name) {
        // Make sure we have a clean lua environment
        luaEnvironment.reset();
        LockoutLogger.debugLog(ChatColor.YELLOW + "Loading board: " + name);

        Optional<BoardInfo> boardInfo = getBoard(name);
        if (boardInfo.isEmpty()) {
            throw new NoSuchElementException("Board " + name + " not found");
        }

        // set lua working directory to the parent folder of the init file.
        luaEnvironment.setEnvironmentPath(Path.of(boardInfo.get().entryPoint()).getParent().toString() + "/");

        BiConsumer<String, String> evalVariable = (String variable, String value) -> {
            // make sure that info table is populated with strings
            String[] identifiers = variable.split("\\.");
            if (identifiers.length > 1 && "info".equals(identifiers[0])) {
                value = "\"" + value + "\"";
            }
            //LockoutLogger.debugLog(variable + " = " + value);
            luaEnvironment.loadString(plugin.getServer().getConsoleSender(), variable + " = " + value);
        };


        LockoutLogger.debugLog("Injecting variables");
        Set<String> initTables = new HashSet<>();
        boardInfo.get().variables()
                .forEach((key, value) -> {
                    String[] tables = getTableKeys(key);

                    // make sure all tables are initialized before assigning values to them
                    String currentTable = tables[0];
                    for (int i = 1; i < tables.length; i++) {
                        if (!initTables.contains(currentTable)) {
                            LockoutLogger.debugLog(String.format("Initializing table '%s'", currentTable));

                            initTables.add(currentTable);
                            evalVariable.accept(currentTable, "{}"); // set binding to empty table
                            currentTable = String.join(".", tables[i]); // join with the next key
                        }
                    }

                    evalVariable.accept(key, value);
                });


        // initialize 'LockoutBoard' table
        luaEnvironment.loadString(plugin.getServer().getConsoleSender(), "LockoutBoard = {}");

        String relativeInitPath = Path.of(luaEnvironment.getEnvironmentPath())
                .relativize(Path.of(boardInfo.get().entryPoint())).toString();

        // Load board files
        luaEnvironment.requireFile(relativeInitPath);

        // Get resulting board definition
        Optional<BoardDefinition> boardDefinition = createBoardDefinition(luaEnvironment.getBoardDefinitionTable());
        boardDefinition.ifPresentOrElse(this::applyBoardDefinition,
                () -> LockoutLogger.warning(String.format("Missing required table 'LockoutBoard' in board '%s'. You cannot add tasks without it!", name)));


    }

    private void applyBoardDefinition(BoardDefinition boardDefinition) {

        // TODO needs several TaskCollections

        // Insert board definitions
        boardDefinition.tasks().forEach(taskManager::addTask);
        boardDefinition.tieBreakCounters().forEach(taskManager::addTieBreakCounter);
        boardDefinition.mutators().forEach(taskManager::addMutator);

        //LockoutLogger.debugLog(ChatColor.GREEN + "Loaded board: " + name);
    }

    @Override
    public Optional<BoardDefinition> getCurrentBoardDefinition() {
        return Optional.empty();
    }

    @Override
    public boolean hasCurrentBoardDefinition() {
        return hasBoardDefinition;
    }

    private Optional<BoardDefinition> createBoardDefinition(LuaTable boardTable) {
        LuaValue luaTasks = boardTable.get("Tasks");
        if (!luaTasks.istable()) {
            LockoutLogger.error("Tasks table does not exist");
            return Optional.empty();
        }

        List<TaskComponent> tasks = new ArrayList<>();
        LuaTable taskTable = luaTasks.checktable();
        // Lua tables are '1 indexed'
        for (int i = 0; i < taskTable.length(); i++) {
            try {
                TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(taskTable.get(i + 1), TaskComponent.class);
                tasks.add(task);
            } catch (LuaError error) {
                LockoutLogger.error("Error while creating task in 'Task' table at index " + i + ": " + error.getMessage());
            }
        }

        List<TaskComponent> tieBreakCounters = new ArrayList<>();
        LuaValue luaTieBreakers = boardTable.get("TieBreakers");
        if (luaTieBreakers.istable()) {
            LuaTable tieBreakerTable = luaTieBreakers.checktable();
            for (int i = 0; i < tieBreakerTable.length(); i++) {
                try {
                    TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(tieBreakerTable.get(i + 1), TaskComponent.class);
                    tieBreakCounters.add(task);
                } catch (LuaError error) {
                    LockoutLogger.error("Error while adding tie breaker from 'TieBreakers' table at index " + i + ": " + error.getMessage());
                }
            }
        }

        List<TaskComponent> mutators = new ArrayList<>();
        LuaValue luaMutators = boardTable.get("Mutators");
        if (luaMutators.istable()) {
            LuaTable mutatorTable = luaMutators.checktable();
            for (int i = 0; i < mutatorTable.length(); i++) {
                try {
                    TaskComponent task = (TaskComponent) CoerceLuaToJava.coerce(mutatorTable.get(i + 1), TaskComponent.class);
                    mutators.add(task);
                } catch (LuaError error) {
                    LockoutLogger.error("Error while adding mutator from 'Mutators' table at index " + i + ": " + error.getMessage());
                }
            }
        }

        // TODO add settings table
        return Optional.of(new BoardDefinition(tasks, tieBreakCounters, mutators, null));
    }

    private String[] getTableKeys(String key) {
        String[] tables = key.split("\\.");

        if (tables.length == 1) {
            throw new InvalidBoardPropertiesException(
                    String.format(
                            "Board variable '%s' must be part of a table such as %s.key=value",
                            tables[0],
                            tables[0]
                    )
            );
        }
        //LockoutLogger.debugLog("Found tables: " + Arrays.toString(tables));
        return tables;
    }

    private BoardInfo createBoardInfo(final Path boardsFile) {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(boardsFile.toFile())) {
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String name = props.getProperty("info.name");
        String author = props.getProperty("info.author");
        String version = props.getProperty("info.version");
        String pluginVersion = props.getProperty("info.pluginVersion");
        String init = props.getProperty("info.init");
        String description = props.getProperty("info.description");
        String compatibility = props.getProperty("info.compatibility");

        // Insert lua variables
        Map<String, String> variables = new HashMap<>();
        props.keySet().forEach(key -> {
            if (key instanceof String keyString) {
                variables.put(keyString, props.getProperty(keyString));
            }
        });

        return new BoardInfo(
                name,
                author,
                version,
                pluginVersion,
                compatibility,
                init,
                description,
                variables);
    }

    private void findAndRegisterBoard(final Path directory) {
        Path boardsFile = directory.resolve("board.properties");

        if (Files.exists(boardsFile)) {
            LockoutLogger.debugLog("Found existing board file: " + boardsFile);
            BoardInfo boardInfo = createBoardInfo(boardsFile);
            boards.add(boardInfo);
        }
    }

    /**
     * Asynchronously searches plugin data folder for directories containing a board.properties file
     * and caches their metadata.
     *
     * @author m-rr
     * @see BoardInfo
     * @since 2.5.1
     * */
    public void registerBoardsAsync() {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            LockoutLogger.debugLog("Registering boards.");
            LockoutLogger.debugLog(boards.stream()
                    .map(Record::toString)
                    .collect(Collectors.joining("\n")));

            try (Stream<Path> dirs = Files.walk(BOARD_PATH)) {
                dirs.filter(Files::isDirectory)
                        .forEach(this::findAndRegisterBoard);
            } catch (IOException e) {
                LockoutLogger.warning(String.format("Error reading boards directory: %s", e.getMessage()));
            }
        });
    }

    /**
     * Clears {@link BoardInfo} cache and resets {@link LuaEnvironment} for next board execution.
     *
     * @author m-rr
     * @since 2.5.1*/
    public void reset() {
        boards.clear();
        luaEnvironment.reset();
    }


    @Override
    public LuaEnvironment getLuaEnvironment() {
        return luaEnvironment;
    }
}
