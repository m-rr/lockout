package stretch.lockout.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import stretch.lockout.game.LockoutContext;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.player.PlayerStat;

import java.time.Instant;
import java.util.*;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder().create();

    public static JsonObject fromString(String json) {
        return gson.fromJson(json, JsonObject.class);
    }
    public static JsonObject fromTask(TimeCompletableTask task, Map<UUID, Integer> uuidObfuscation) {
        JsonObject result = new JsonObject();
        result.addProperty("description", task.getDescription());
        result.addProperty("reward", task.hasReward() ? task.getReward().getDescription() : "nil");
        result.addProperty("value", task.getValue());
        if (task.isCompleted()) {
            result.addProperty("who", uuidObfuscation.get(task.getScoredPlayer().getPlayer().getUniqueId()));
            result.addProperty("time", task.getTimeCompleted().getSeconds());
            result.add("loc", generateLocation(task.getLocation()));
        }

        return result;
    }

    private static <T> Map<T, Integer> obfuscateCollection(Collection<T> items) {
        int i = 0;
        Map<T, Integer> result = new HashMap<>();
        for (var item : items) {
            result.put(item, i);
            i++;
        }
        return ImmutableMap.copyOf(result);
    }

    public static JsonObject generateReport(final LockoutContext lockout) {
        var teams = lockout.getTeamManager().getTeams();
        var uuids = lockout.getTeamManager().getPlayerStats().stream()
                .map(PlayerStat::getPlayer)
                .map(Player::getUniqueId)
                .toList();
        var teamMapping = obfuscateCollection(teams);
        var playerMapping = obfuscateCollection(uuids);
        var uuidTeams = lockout.getTeamManager().getUUIDMappedPlayerStats();
        // team to players
        Map <Integer, Set<Integer>> teamObfuscation = new HashMap<>();
        for (var t : teamMapping.values()) {
            teamObfuscation.put(t, new HashSet<>());
        }
        for (var u : uuids) {
            LockoutTeam theirTeam = uuidTeams.get(u).getTeam();
            teamObfuscation.get(teamMapping.get(theirTeam)).add(playerMapping.get(u));
        }

        JsonObject result = new JsonObject();
        result.addProperty("time", Instant.now().toEpochMilli());
        result.add("config", generateConfig(lockout));
        result.add("game", generateGameInfo(lockout));
        result.add("team", generateTeamInfo(lockout, teamObfuscation.toString()));

        JsonArray tasksArr = new JsonArray();
        var tasks = lockout.getMainTasks().getTasks();
        for (var t : tasks) {
            if (t instanceof TimeCompletableTask completableTask) {
                tasksArr.add(JsonUtil.fromTask(completableTask, playerMapping));
            }
        }

        JsonArray tieArr = new JsonArray();
        var tieTasks = lockout.getTieBreaker().getTasks();
        for (var t : tieTasks) {
            if (t instanceof TimeCompletableTask completableTask) {
                tieArr.add(JsonUtil.fromTask(completableTask, playerMapping));
            }
        }

        result.add("tasks", tasksArr);
        result.add("tiebreak", tieArr);
        return result;
    }

    public static JsonObject generateTeamInfo(final LockoutContext lockout, final String teamMappingData) {
        JsonObject teamInfo = new JsonObject();
        teamInfo.addProperty("teamCount", lockout.getTeamManager().teamCount());
        teamInfo.addProperty("playerCount", (long) lockout.getTeamManager().getPlayerStats().size());
        teamInfo.addProperty("teams", teamMappingData);

        return teamInfo;
    }

    public static JsonObject generateGameInfo(final LockoutContext lockout) {
        JsonObject gameInfo = new JsonObject();

        gameInfo.addProperty("seed", lockout.settings().getGameWorld().getSeed());
        gameInfo.add("spawn", generateLocation(lockout.settings().getGameWorld().getSpawnLocation()));
        gameInfo.addProperty("version", lockout.getPlugin().getDescription().getVersion());
        gameInfo.addProperty("server", lockout.getPlugin().getServer().getVersion());
        gameInfo.addProperty("elapsedTime", lockout.getUiManager().getTimer().elapsedTime().getSeconds());
        gameInfo.addProperty("timeLimit", lockout.getUiManager().getTimer().getTime().getSeconds());

        return gameInfo;
    }

    public static JsonObject generateLocation(final Location loc) {
        JsonObject locJson = new JsonObject();
        locJson.addProperty("world", loc.getWorld().getName());
        locJson.addProperty("x", loc.getX());
        locJson.addProperty("y", loc.getY());
        locJson.addProperty("z", loc.getZ());

        return locJson;
    }

    public static JsonObject generateConfig(final LockoutContext lockout) {
        JsonObject config = new JsonObject();
        config.addProperty("rules", lockout.settings().gameRules().toString());
        config.addProperty("startTime", lockout.settings().getStartTime());
        config.addProperty("countdownTime", lockout.settings().getCountdownTime());
        config.addProperty("defaultTeams", lockout.settings().getDefaultTeams());
        config.addProperty("maxTeams", lockout.settings().getMaxTeams());
        config.addProperty("teamSize", lockout.settings().getTeamSize());
        config.addProperty("taskList", Optional.ofNullable(lockout.getPlugin().getConfig()
                        .getString("autoLoadTask"))
                .orElse("default"));

        return config;
    }
}
