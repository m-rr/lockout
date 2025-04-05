package stretch.lockout.reward.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.reward.api.RewardType;
import stretch.lockout.reward.base.Reward;

public class RewardItem extends Reward {
    final private ItemStack rewardItem;

    public RewardItem(ItemStack rewardItem, String description) {
        super(description);
        this.rewardItem = rewardItem;
    }
    public RewardItem(ItemStack rewardItem, RewardType rewardType, String description) {
        super(rewardType, description);
        this.rewardItem = rewardItem;
    }

    @Override
    public void giveReward(Player player) {
        var playerInv = player.getInventory();
        // Check if inventory is full
        if (playerInv.firstEmpty() == -1) {
            var world = player.getWorld();
            world.dropItem(player.getLocation(), rewardItem);
            return;
        }

        playerInv.addItem(rewardItem);
    }
}
