package stretch.lockout.board;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import stretch.lockout.game.state.LockoutSettings;
import stretch.lockout.lua.LuaEnvironment;
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

public class FileBasedBoardManager implements BoardManager {
    private final Path BOARD_PATH = Paths.get("plugins/Lockout/");
    LuaEnvironment luaEnvironment;
    private final LockoutSettings settings;
    private final Plugin plugin;
    private final List<BoardInfo> boards = new ArrayList<>();

    public FileBasedBoardManager(Plugin plugin, LockoutSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.luaEnvironment = new LuaEnvironment(plugin, settings);
    }

    public List<BoardInfo> getBoards() {
        return boards;
    }

    public Optional<BoardInfo> getBoard(final String name) {
        return getBoards().stream()
                .filter(board -> board.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public void loadBoard(final String boardName) {
        luaEnvironment.resetTables();
        LockoutLogger.debugLog("Loading board " + boardName);

        Optional<BoardInfo> boardInfo = getBoard(boardName);
        if (boardInfo.isEmpty()) {
            throw new InvalidBoardPropertiesException("Board " + boardName + " not found");
        }

        // set lua working directory to the parent folder of the init file.
        luaEnvironment.setEnvironmentPath(Path.of(boardInfo.get().entryPoint()).getParent().toString() + "/");

        BiConsumer<String, String> evalVariable = (String variable, String value) -> {
            // make sure that info table is populated with strings
            String[] identifiers = variable.split("\\.");
            if (identifiers.length > 1 && "info".equals(identifiers[0])) {
                value = "\"" + value + "\"";
            }
            LockoutLogger.debugLog(variable + " = " + value);
            luaEnvironment.loadString(plugin.getServer().getConsoleSender(), variable + " = " + value);
        };


        LockoutLogger.debugLog("Injecting variables");
        // inject variables
        Set<String> initTables = new HashSet<>();
        boardInfo.get().variables()
                .forEach((key, value) -> {
                    String[] tables = getStrings(key);

                    // make sure all tables are initialized before assigning values
                    String currentTable = tables[0];
                    for (int i = 1; i < tables.length; i++) {
                        if (!initTables.contains(currentTable)) {
                            LockoutLogger.debugLog(String.format("Initializing table '%s'", currentTable));

                            initTables.add(currentTable);
                            evalVariable.accept(currentTable, "{}");
                            currentTable = String.join(".", tables[i]);
                        }
                    }

                    evalVariable.accept(key, value);
                });


        String relativeInitPath = Path.of(luaEnvironment.getEnvironmentPath())
                .relativize(Path.of(boardInfo.get().entryPoint())).toString();

        luaEnvironment.requireFile(relativeInitPath);
        LockoutLogger.debugLog(String.format("Board %s loaded", boardName));
    }

    private String[] getStrings(String key) {
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
        LockoutLogger.debugLog("Found tables: " + Arrays.toString(tables));
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
                throw new RuntimeException(e);
            }
        });
    }

    public void reset() {
        boards.clear();
        luaEnvironment.resetTables();
    }

    @Override
    public LuaEnvironment getLuaEnvironment() {
        return luaEnvironment;
    }
}
