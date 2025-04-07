package stretch.lockout.reward.scheduler; // Or a suitable package

import stretch.lockout.reward.api.RewardComponent;

import java.util.UUID;

/**
 * Stores information about a reward that needs to be applied to a player
 * who was offline when it was originally scheduled to be delivered.
 *
 * @param playerUuid The UUID of the player to receive the reward.
 * @param reward     The RewardComponent to apply.
 */
public record PendingReward(UUID playerUuid, RewardComponent reward) {

}

