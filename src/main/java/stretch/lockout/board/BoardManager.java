package stretch.lockout.board;

import org.jetbrains.annotations.NotNull;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.lua.LuaEnvironment;
import stretch.lockout.util.MessageUtil;

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

public class BoardManager {
    private final Path BOARD_PATH = Paths.get("plugins/Lockout/");
    LuaEnvironment luaEnvironment;
    private final LockoutContext lockoutContext;
    private final List<BoardInfo> boards = new ArrayList<>();

    public BoardManager(final LockoutContext lockoutContext) {
        this.lockoutContext = lockoutContext;
        this.luaEnvironment = new LuaEnvironment(lockoutContext);
    }

    public List<BoardInfo> getBoards() {
        return boards;
    }

    public Optional<BoardInfo> getBoard(final String name) {
        return getBoards().stream()
                .filter(board -> board.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public void loadBoard(String boardName) {
        luaEnvironment.resetTables();
        MessageUtil.debugLog(lockoutContext.settings(), "Loading board " + boardName);

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
            MessageUtil.debugLog(lockoutContext.settings(), variable + " = " + value);
            luaEnvironment.loadString(lockoutContext.getPlugin().getServer().getConsoleSender(), variable + " = " + value);
        };


        MessageUtil.debugLog(lockoutContext.settings(), "Injecting variables");
        // inject variables
        Set<String> initTables = new HashSet<>();
        boardInfo.get().variables()
                .forEach((key, value) -> {
                    String[] tables = getStrings(key);

                    // make sure all tables are initialized before assigning values
                    String currentTable = tables[0];
                    for (int i = 1; i < tables.length; i++) {
                        if (!initTables.contains(currentTable)) {
                            MessageUtil.debugLog(lockoutContext.settings(), String.format("Initializing table '%s'", currentTable));

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
        MessageUtil.debugLog(lockoutContext.settings(), String.format("Board %s loaded", boardName));
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
        MessageUtil.debugLog(lockoutContext.settings(), "Found tables: " + Arrays.toString(tables));
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
            MessageUtil.debugLog(lockoutContext.settings(), "Found existing board file: " + boardsFile);
            BoardInfo boardInfo = createBoardInfo(boardsFile);
            boards.add(boardInfo);
        }
    }

    public void registerBoards() {
        MessageUtil.debugLog(lockoutContext.settings(), "Registering boards.");
        MessageUtil.debugLog(lockoutContext.settings(), boards.stream()
                .map(Record::toString)
                .collect(Collectors.joining("\n")));

        try (Stream<Path> dirs = Files.walk(BOARD_PATH)) {
            dirs.filter(Files::isDirectory)
                    .forEach(this::findAndRegisterBoard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        boards.clear();
        luaEnvironment.resetTables();
    }
}
