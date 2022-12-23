package stretch.lockout.reward;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class RewardPotion extends RewardLeaf{
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
