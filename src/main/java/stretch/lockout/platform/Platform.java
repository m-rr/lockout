package stretch.lockout.platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import stretch.lockout.util.JsonUtil;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Platform {
    public static @Nullable String latestUpdate() {
        try {
            URL url = new URL(Resource.LOCKOUT_INFO_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.setDoOutput(true);
            connection.addRequestProperty("User-Agent", "Lockout");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder builder = new StringBuilder();
            while ((input = br.readLine()) != null) {
                builder.append(input);
            }
            br.close();
            JsonObject stats;
            try {
                stats = JsonUtil.fromString(builder.toString());
            }
            catch (JsonParseException ignored) {
                return null;
            }
            return stats.get("name").getAsString();
        }
        catch (IOException ignored) {
            return null;
        }
    }

    public static void collectReport(JsonObject report) {
        try {
            URL url = new URL(Resource.LOCKOUT + "/match");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("User-Agent", "Lockout");
            connection.addRequestProperty("Content-Type", "application/json");

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(report.toString());
            wr.flush();

            final int responseCode = connection.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder builder = new StringBuilder();
            while ((input = br.readLine()) != null) {
                builder.append(input);
            }
            br.close();
            JsonObject response;
            try {
                response = JsonUtil.fromString(builder.toString());
            }
            catch (JsonParseException ignored) {

            }
            connection.disconnect();
        }
        catch (IOException ignored) {

        }
    }

    public static String getSlurp() {
        try {
            URL url = new URL("http://192.168.7.52:4000/slurp");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.setDoOutput(true);
            connection.addRequestProperty("User-Agent", "Lockout");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder builder = new StringBuilder();
            while ((input = br.readLine()) != null) {
                builder.append(input);
            }
            br.close();
            JsonObject json;
            try {
                json = JsonUtil.fromString(builder.toString());
            }
            catch (JsonParseException ignored) {
                return null;
            }

            return json.get("tasks").getAsString();
        }
        catch (IOException ignored) {
            return null;
        }
    }

    public static class Resource {
        public static final String LOCKOUT_INFO_URL = "https://api.spiget.org/v2/resources/112607/versions/latest";
        public static final String LOCKOUT_DOWNLOAD_URL = "https://www.spigotmc.org/resources/lockout.112607/";
        public static final String LOCKOUT = "http://lockout.goodstretch.net";
    }
}
