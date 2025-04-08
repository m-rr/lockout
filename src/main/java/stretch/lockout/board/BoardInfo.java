package stretch.lockout.board;

import java.util.Map;

/**
 * Contains information about a discovered board.
 *
 * @author m-rr
 * @version @projectVersion@
 * @see BoardManager
 * @see FileBasedBoardManager
 * @since 2.5.1
 * */
public record BoardInfo(String name,
                        String author,
                        String boardVersion,
                        String pluginVersion,
                        String minecraftVersion,
                        String entryPoint,
                        String description,
                        Map<String, String> variables) {
}
