package stretch.lockout.game.state;

import stretch.lockout.game.LockoutContext;
import stretch.lockout.util.MessageUtil;

public abstract class GameStateHandler {
    protected GameState gameState;
    protected final LockoutContext lockout;

    protected GameStateHandler(final LockoutContext lockout) {
        this.lockout = lockout;
        gameState = GameState.UNINIT;
    }

    public GameState getGameState() {return gameState;}
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        MessageUtil.debugLog(lockout.settings(), "Entering gamestate " + gameState.name());
        switch (gameState) {
            case PRE -> preGame();
            case READY -> ready();
            case STARTING -> starting();
            case RUNNING -> running();
            case TIEBREAKER -> tiebreaker();
            case PAUSED -> paused();
            case END -> {
                lockout.reset();
                endGame();
            }
        }
    }

    protected abstract void preGame();
    protected abstract void ready();
    protected abstract void starting();
    protected abstract void running();
    protected abstract void tiebreaker();
    // No need to manually reinit GameContext members; it is handled for you.
    protected abstract void endGame();
    protected abstract void paused();
}
