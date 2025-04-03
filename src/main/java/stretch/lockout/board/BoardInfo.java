package stretch.lockout.board;

import java.nio.file.Path;
import java.util.Map;

public record BoardInfo(String name,
                        String author,
                        String boardVersion,
                        String pluginVersion,
                        String minecraftVersion,
                        String entryPoint,
                        String description,
                        Map<String, String> variables) {
}
