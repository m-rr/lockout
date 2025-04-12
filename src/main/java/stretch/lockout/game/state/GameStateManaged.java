package stretch.lockout.game.state;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import stretch.lockout.event.GameOverEvent;
import stretch.lockout.event.ReadyGameEvent;
import stretch.lockout.event.ResetGameEvent;
import stretch.lockout.event.StartGameEvent;

import java.util.Objects;

public abstract class GameStateManaged implements Listener {
    protected final Plugin plugin;
    public GameStateManaged(@NonNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onReadyGameEvent(ReadyGameEvent event) {
        onReady(event);
    }

    @EventHandler
    public void onGameOverEvent(GameOverEvent event) {
        onGameOver(event);
    }

    @EventHandler
    public void onStartGameEvent(StartGameEvent event) {
        onStart(event);
    }

    @EventHandler
    public void onResetGameEvent(ResetGameEvent event) {
        onReset(event);
    }

    public void onReady(ReadyGameEvent event) {}
    public void onGameOver(GameOverEvent event) {}
    public void onStart(StartGameEvent event) {}
    public void onReset(ResetGameEvent event) {}
}
