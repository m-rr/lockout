package stretch.lockout.reward.impl;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.reward.base.Reward;

public class RewardPotion extends Reward {
    final private PotionEffect potionEffect;
    public RewardPotion(PotionEffect potionEffect, String description) {
        super(description);
        this.potionEffect = potionEffect;
    }

    public RewardPotion(PotionEffect potionEffect, RewardType rewardType, String description) {
        super(rewardType, description);
        this.potionEffect = potionEffect;
    }

    @Override
    public void giveReward(Player player) {
        player.addPotionEffect(potionEffect);
    }
}
