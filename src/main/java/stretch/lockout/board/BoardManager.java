package stretch.lockout.board;

import stretch.lockout.lua.LuaEnvironment;

import java.util.List;
import java.util.Optional;

public interface BoardManager {
    List<BoardInfo> getBoards();
    Optional<BoardInfo> getBoard(String id);
    void loadBoard(String boardName);
    void registerBoardsAsync();
    void reset();
    LuaEnvironment getLuaEnvironment();
}
