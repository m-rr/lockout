package stretch.lockout.task.composite;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import stretch.lockout.event.executor.LockoutWrappedEvent;
import stretch.lockout.reward.api.RewardComponent;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.team.player.PlayerStat;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

public sealed abstract class TaskComposite implements TimeCompletableTask permits TaskSet, TaskChoice, TaskSequence {
    final protected List<TaskComponent> taskComponents = new ArrayList<>();
    final protected HashMap<HumanEntity, HashSet<TaskComponent>> playerCompletedTasks = new HashMap<>();
    protected int value;
    final protected HashSet<Class<? extends Event>> eventClassSet = new HashSet<>();
    protected String descriptionEntryPrefix;
    protected String description;
    protected ItemStack guiItemStack;
    protected RewardComponent reward;
    protected PlayerStat scoredPlayer;
    protected Duration timeCompleted;
    protected Location location;

    public TaskComposite(int value) {
        this.value = value;
    }

    public TaskComposite(List<TaskComponent> taskComponents, int value) {
        this.value = value;
        taskComponents.forEach(this::addTaskComponent);
    }

    public TaskComposite(List<TaskComponent> taskComponents, int value, String description) {
        this.value = value;
        this.description = description;
        taskComponents.forEach(this::addTaskComponent);
    }

    @Override
    public TaskComponent setGuiItemStack(ItemStack itemStack) {
        guiItemStack = itemStack;
        return this;
    }

    @Override
    public boolean hasGuiItemStack() {
        return guiItemStack != null;
    }

    @Override
    public ItemStack getGuiItemStack() {
        return guiItemStack;
    }

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
            description = getDescriptionRecursive();
        }

        return description;
    }

    @Override
    public TaskComponent setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public TaskComponent addPlayerCondition(Predicate<HumanEntity> predicate) {
        taskComponents.forEach(taskComponent -> taskComponent.addPlayerCondition(predicate));
        return this;
    }

    @Override
    public TaskComponent addPlayerCondition(LuaValue predicate) {
        taskComponents.forEach(taskComponent -> taskComponent.addPlayerCondition(predicate));
        return this;
    }

    public void setDescriptionEntryPrefix(String str) {
        this.descriptionEntryPrefix = str;
    }

    @Override
    public Set<Class<? extends Event>> getEventClasses() {
        return eventClassSet;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public TaskComponent setValue(final int value) {
        this.value = value;
        return this;
    }

    @Override
    public Material getDisplay() {
        return hasGuiItemStack() ? getGuiItemStack().getType() : Material.AIR;
    }

    @Override
    public TaskComponent setDisplay(Material display) {
        setGuiItemStack(new ItemStack(display));
        return this;
    }

    @Override
    public RewardComponent getReward() {
        return reward;
    }

    @Override
    public TaskComponent setReward(RewardComponent rewardComponent) {
        this.reward = rewardComponent;
        return this;
    }

    @Override
    public boolean hasReward() {
        return reward != null;
    }

    @Override
    public boolean isCompleted() {
        return scoredPlayer != null;
    }

    @Override
    public PlayerStat getScoredPlayer() {
        return scoredPlayer;
    }

    @Override
    public void setTimeCompleted(Duration time) {
        this.timeCompleted = time;
    }

    @Override
    public Duration getTimeCompleted() {
        return this.timeCompleted;
    }

    @Override
    public void setLocation(Location loc) {
        this.location = loc;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setCompletedBy(PlayerStat scoringPlayer) {
        this.scoredPlayer = scoringPlayer;
    }

    @Override
    public boolean doesAccomplish(final LockoutWrappedEvent lockoutEvent) {
        Optional<Player> optionalPlayer = lockoutEvent.getPlayer();
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();

        for (TaskComponent taskComponent : taskComponents) {
            if (taskComponent.doesAccomplish(lockoutEvent)) {
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
