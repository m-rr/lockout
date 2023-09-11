package stretch.lockout.tracker;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.util.MessageUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class PlayerTracker implements Runnable {
    private final Map<Player, IntSupplier> trackingCycle = new HashMap<>();
    private final Map<Player, CircleLocationSupplier> circleCycle = new HashMap<>();
    private final Map<Player, Player> trackingSelection = new HashMap<>();
    private List<PlayerStat> players;

    public void changeTracker(Player player) {
        if (players.stream().map(playerStat -> playerStat.getPlayer().isOnline()).count() < 2) {
            MessageUtil.sendActionBar(player, ChatColor.RED + "No other players to track.");
            return;
        }

        int index = trackingCycle.get(player).getAsInt();
        Player targetPlayer = players.get(index).getPlayer();
        while (targetPlayer == player || !targetPlayer.isOnline()) {
            index = trackingCycle.get(player).getAsInt();
            targetPlayer = players.get(index).getPlayer();
        }

        trackingSelection.put(player, targetPlayer);
        String message = targetPlayer.getWorld().getEnvironment() == World.Environment.NORMAL ?
                ChatColor.GOLD + "Now tracking " + ChatColor.BLUE + targetPlayer.getName() :
                ChatColor.BLUE + targetPlayer.getName() + ChatColor.GOLD + " is not in the overworld";
        MessageUtil.sendActionBar(player, message);
    }

    public void update() {
        players.stream()
                .map(PlayerStat::getPlayer)
                .forEach(player -> {
                    if (player.isOnline()) {
                        Player targetPlayer = trackingSelection.get(player);
                        if (targetPlayer != null && targetPlayer.getWorld() == player.getWorld()) {
                            player.setCompassTarget(targetPlayer.getLocation());
                        }
                        else {
                            player.setCompassTarget(circleCycle.get(player).get());
                        }
                    }
                });
    }

    public void setPlayer(PlayerStat playerStat) {
        Player player = playerStat.getPlayer();
        trackingCycle.put(player, new IndexSupplier());
        circleCycle.put(player, new CircleLocationSupplier(player));
        trackingSelection.put(player, null);
    }

    public void setAllPlayers(Collection<PlayerStat> players) {
        this.players = players.stream().toList();
        for (PlayerStat playerStat : players) {
            setPlayer(playerStat);
        }
    }

    public Player getTrackedPlayer(Player player) {
        return trackingSelection.getOrDefault(player, player);
    }

    @Override
    public void run() {
        update();
    }

    private class CircleLocationSupplier implements Supplier<Location> {
        private final Player player;
        private final double offset = 0.25D;
        private final double scalar = 100D;
        private double loc = 0D;

        public CircleLocationSupplier(Player player) {
            this.player = player;
        }

        @Override
        public Location get() {
            if (loc > 2 * Math.PI) {
                loc = 0D;
            }

            loc += offset;
            double yaw = Math.toRadians(player.getLocation().getYaw());
            double x = Math.cos(loc + yaw) * scalar;
            double z = Math.sin(loc + yaw) * scalar;

            return player.getLocation().add(x, 0D, z);
        }
    }

    private class IndexSupplier implements IntSupplier {
        private int index = -1;

        @Override
        public int getAsInt() {
            index = index + 1;
            return index % players.size();
        }
    }
}
