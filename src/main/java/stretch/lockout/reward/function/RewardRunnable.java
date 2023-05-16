package stretch.lockout.reward.function;

import stretch.lockout.reward.RewardComponent;

public interface RewardRunnable extends Runnable {
    RewardComponent getReward();
}
