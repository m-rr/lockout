package stretch.lockout.board;

import stretch.lockout.lua.LuaEnvironment;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface needed to achieve the loading and retrieval of {@link BoardInfo} metadata.
 *
 * @author m-rr
 * @version @projectVersion@
 * @see BoardInfo
 * @see FileBasedBoardManager
 * @see InvalidBoardPropertiesException
 * @since 2.5.1
 * */
public interface BoardManager {
    List<BoardInfo> getBoards();
    Optional<BoardInfo> getBoard(String id);
    void loadBoard(String boardName);
    void registerBoardsAsync();
    void reset();
    LuaEnvironment getLuaEnvironment();
}
