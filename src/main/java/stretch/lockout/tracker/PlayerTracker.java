package stretch.lockout.tracker;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import stretch.lockout.team.PlayerStat;
import stretch.lockout.util.MessageUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public class PlayerTracker implements Runnable {
    private final Map<Player, IntSupplier> trackingCycle = new HashMap<>();
    private final Map<Player, Player> trackingSelection = new HashMap<>();
    private List<PlayerStat> players;

    public void changeTracker(Player player) {
        if (players.stream().map(playerStat -> playerStat.getPlayer().isOnline()).count() < 2) {
            MessageUtil.sendActionBar(player, ChatColor.RED + "You are the only player online");
            return;
        }

        int index = trackingCycle.get(player).getAsInt();
        Player targetPlayer = players.get(index).getPlayer();
        while (targetPlayer == player || !targetPlayer.isOnline()) {
            index = trackingCycle.get(player).getAsInt();
            targetPlayer = players.get(index).getPlayer();
        }

        player.setCompassTarget(targetPlayer.getLocation());
        trackingSelection.put(player, targetPlayer);
        MessageUtil.sendActionBar(player, ChatColor.GOLD + "Now tracking " + ChatColor.BLUE + targetPlayer.getName());
    }

    public void update() {
        players.stream()
                .map(PlayerStat::getPlayer)
                .forEach(player -> {
                    if (player.isOnline()) {
                        Player targetPlayer = trackingSelection.get(player);
                        if (targetPlayer != null) {
                            player.setCompassTarget(targetPlayer.getLocation());
                        }
                    }
                });
    }

    public void setPlayers(Collection<PlayerStat> players) {
        this.players = players.stream().toList();
        for (PlayerStat playerStat : players) {
            Player player = playerStat.getPlayer();
            trackingCycle.put(player, new IndexSupplier());
            trackingSelection.put(player, null);
        }
    }

    @Override
    public void run() {
        update();
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
