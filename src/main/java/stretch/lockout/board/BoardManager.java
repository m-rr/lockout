package stretch.lockout.board;

import stretch.lockout.game.state.StateResettable;
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
public interface BoardManager extends StateResettable {
    List<BoardInfo> getBoards();
    Optional<BoardInfo> getBoard(String id);
    void loadBoard(String boardName);
    Optional<BoardDefinition> getCurrentBoardDefinition();
    boolean hasCurrentBoardDefinition();
    void registerBoardsAsync();
    //void reset();
    LuaEnvironment getLuaEnvironment();
}
