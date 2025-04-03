package stretch.lockout.task.special;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.task.Task;
import stretch.lockout.util.ArmorStandUtil;

import java.util.Arrays;

/*
* This class does not get used, but I am leaving it.
* */
public class TaskArmorStand extends Task {
    final private ItemStack[] taskArmorStandContents;
    public TaskArmorStand(Class<? extends Event> eventClass, int value, ItemStack[] itemStack, String description) {
        super(eventClass, value, description);
        taskArmorStandContents = itemStack;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        /*if (event instanceof PlayerArmorStandManipulateEvent armorStandManipulateEvent) {
            ItemStack[] armorStandContents = armorStandManipulateEvent.getRightClicked().getEquipment().getArmorContents();
            ItemStack playerItem = armorStandManipulateEvent.getPlayerItem();

            var afterArmorStandSwap = ArmorStandUtil.addArmorStandItem(armorStandContents, playerItem);

            return Arrays.equals(afterArmorStandSwap, taskArmorStandContents);
        }

        return false;*/
        throw new UnsupportedOperationException();
    }
}
