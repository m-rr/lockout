/*
 * @From
 * https://github.com/kangarko/CowCannon/blob/master/src/main/java/org/mineacademy/cowcannon/model/Toast.java
 */

package stretch.lockout.ui.advancement;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import stretch.lockout.Lockout;
import stretch.lockout.task.api.TaskComponent;

import java.util.UUID;

@Deprecated
public class PlayerAdvancementMessage {
    private final NamespacedKey key;
    private final Material guiMaterial;
    private final String message;

    public PlayerAdvancementMessage(final Material guiMaterial, final String message) {
        this.key = new NamespacedKey(Lockout.getPlugin(Lockout.class), UUID.randomUUID().toString());
        this.guiMaterial = guiMaterial;
        this.message = message;
        createAdvancement();
    }

    public PlayerAdvancementMessage(final TaskComponent task) {
        this(task.getGuiItemStack().getType(), task.getDescription());
    }

    public void sendMessage(final Player player) {
        //createAdvancement();
        grantAdvancement(player);

        Bukkit.getScheduler().runTaskLater(Lockout.getPlugin(Lockout.class), () -> revokeAdvancement(player), 10L);
    }

    private void grantAdvancement(final Player player) {
        player.getAdvancementProgress(Bukkit.getAdvancement(key)).awardCriteria("trigger");
    }

    private void revokeAdvancement(final Player player) {
        player.getAdvancementProgress(Bukkit.getAdvancement(key)).revokeCriteria("trigger");
        player.sendMessage("debug" + " revoked advancement");
    }

    private void createAdvancement() {
        Bukkit.getUnsafe().loadAdvancement(key, "{\n" +
				"    \"criteria\": {\n" +
				"        \"trigger\": {\n" +
				"            \"trigger\": \"minecraft:impossible\"\n" +
				"        }\n" +
				"    },\n" +
				"    \"display\": {\n" +
				"        \"icon\": {\n" +
				"            \"id\": \"minecraft:" + "sand" + "\"\n" +
				"        },\n" +
				"        \"title\": {\n" +
				"            \"text\": \"" + message.replace("|", "\n") + "\"\n" +
				"        },\n" +
				"        \"description\": {\n" +
				"            \"text\": \"\"\n" +
				"        },\n" +
				"        \"background\": \"minecraft:textures/gui/advancements/backgrounds/adventure.png\",\n" +
				"        \"announce_to_chat\": true,\n" +
				"        \"show_toast\": true,\n" +
                "        \"frame\": \"" + "goal" + "\",\n" +
				"        \"hidden\": false\n" +
				"    },\n" +
				"    \"requirements\": [\n" +
				"        [\n" +
				"            \"trigger\"\n" +
				"        ]\n" +
				"    ]\n" +
				"}");
    }
}
