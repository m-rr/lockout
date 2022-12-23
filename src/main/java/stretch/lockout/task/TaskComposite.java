package stretch.lockout.task;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import stretch.lockout.reward.RewardComponent;
import stretch.lockout.team.PlayerStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public sealed abstract class TaskComposite implements TaskComponent permits TaskANDComposite, TaskORComposite {
    final protected List<TaskComponent> taskComponents = new ArrayList<>();
    final protected HashMap<HumanEntity, HashSet<TaskComponent>> playerCompletedTasks = new HashMap<>();
    final protected int value;
    final protected HashSet<Class> eventClassSet = new HashSet<>();
    protected String descriptionEntryPrefix;
    protected String description;
    protected ItemStack guiItemStack;
    protected RewardComponent reward;
    protected PlayerStat scoredPlayer;
    public TaskComposite(int value) {
        this.value = value;
    }

    public TaskComposite(List<TaskComponent> taskComponents, int value) {
        this.value = value;
        taskComponents.forEach(this::addTaskComponent);
    }

    @Override
    public TaskComponent setGuiItemStack(ItemStack itemStack) {
        guiItemStack = itemStack;
        return this;
    }

    @Override
    public boolean hasGuiItemStack() {return guiItemStack != null;}

    @Override
    public ItemStack getGuiItemStack() {return guiItemStack;}

    private String getDescriptionRecursive() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (int i = 0; i < taskComponents.size(); i++) {
            result.append(taskComponents.get(i).getDescription());
            if (i + 1 != taskComponents.size()) {
                result.append(descriptionEntryPrefix);
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String getDescription() {
        if (description == null) {
            return getDescriptionRecursive();
        }

        return description;
    }

    public void setDescriptionEntryPrefix(String str) {
        this.descriptionEntryPrefix = str;
    }

    @Override
    public HashSet<Class> getEventClasses() {
        return eventClassSet;
    }

    @Override
    public int getValue() {return value;}

    @Override
    public RewardComponent getReward() {return reward;}

    @Override
    public TaskComponent setReward(RewardComponent rewardComponent) {
        this.reward = rewardComponent;
        return this;
    }

    @Override
    public boolean hasReward() {return reward != null;}

    @Override
    public boolean isCompleted() {return scoredPlayer != null;}

    @Override
    public PlayerStat getScoredPlayer() {return scoredPlayer;}

    @Override
    public void setCompletedBy(PlayerStat scoringPlayer) {
        this.scoredPlayer = scoringPlayer;
    }

    @Override
    public boolean doesAccomplish(HumanEntity player, Event event) {
        for (TaskComponent taskComponent : taskComponents) {
            if (taskComponent.doesAccomplish(player, event)) {
                setPlayerCompletedTasks(player, taskComponent);
            }
        }

        return playerCompletedTask(player);
    }

    public abstract boolean playerCompletedTask(HumanEntity player);

    public void addTaskComponent(TaskComponent taskComponent) {
        taskComponents.add(taskComponent);
        eventClassSet.addAll(taskComponent.getEventClasses());
    }

    public void setPlayerCompletedTasks(HumanEntity player, TaskComponent taskComponent) {
        if (!playerCompletedTasks.containsKey(player)) {
            playerCompletedTasks.put(player, new HashSet<>());
        }
        var completedTasks = playerCompletedTasks.get(player);
        completedTasks.add(taskComponent);
    }

}
