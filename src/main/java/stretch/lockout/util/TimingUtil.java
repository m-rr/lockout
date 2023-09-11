package stretch.lockout.util;

import org.bukkit.Bukkit;

public class TimingUtil {
    public static void timeMethod(Runnable function) {
        long start = System.nanoTime();
        function.run();
        long end = System.nanoTime();
        long diff = end - start;
        Bukkit.getConsoleSender().sendMessage("Elapsed milliseconds for method: " + diff / 1000000);
    }
}
