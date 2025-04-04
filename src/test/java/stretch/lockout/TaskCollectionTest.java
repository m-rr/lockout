package stretch.lockout;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;
import stretch.lockout.task.base.Task;
import stretch.lockout.task.api.TaskComponent;
import stretch.lockout.task.HiddenTask;
import stretch.lockout.task.composite.TaskChoice; // Example composite
import stretch.lockout.task.api.TimeCompletableTask;
import stretch.lockout.task.manager.TaskCollection;
import stretch.lockout.team.LockoutTeam;
import stretch.lockout.team.player.PlayerStat;


import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


class TaskCollectionTest {

    private ServerMock server;
    private Lockout plugin;
    private TaskCollection taskCollection;

    // Helper method to create a basic task
    private Task createTask(Class<? extends Event> eventClass, int value, String desc) {
        return new Task(eventClass, value, desc);
    }

    // Helper method to create a composite task (OR for testing multiple events)
    private TaskChoice createOrComposite(TaskComponent task1, TaskComponent task2, int value, String desc) {
        return new TaskChoice(List.of(task1, task2), value, desc);
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        //plugin = MockBukkit.load(Lockout.class); // Load plugin if needed by tasks, though likely not for TaskCollection itself
        taskCollection = new TaskCollection();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testAddTask_SingleEvent() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        taskCollection.addTask(task1);

        assertTrue(taskCollection.isTasksLoaded());
        assertEquals(1, taskCollection.getTaskCount());
        assertEquals(1, taskCollection.getTasks().size());
        assertTrue(taskCollection.getTasks().contains(task1));
        assertEquals(1, taskCollection.getEventClasses().size());
        assertTrue(taskCollection.getEventClasses().contains(PlayerJoinEvent.class));
        assertNotNull(taskCollection.getMappedTasks().get(PlayerJoinEvent.class));
        assertEquals(1, taskCollection.getMappedTasks().get(PlayerJoinEvent.class).size());
        assertTrue(taskCollection.getMappedTasks().get(PlayerJoinEvent.class).contains(task1));
    }

    @Test
    void testAddTask_InvisibleTask() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        HiddenTask invisibleTask = new HiddenTask(task1); // Wrap a real task
        taskCollection.addTask(invisibleTask);

        assertTrue(taskCollection.isTasksLoaded());
        assertEquals(0, taskCollection.getTaskCount(), "Invisible tasks should not count towards task count");
        assertEquals(1, taskCollection.getTasks().size()); // Should still be in the main set
        assertTrue(taskCollection.getTasks().contains(invisibleTask));
        assertEquals(1, taskCollection.getEventClasses().size());
        assertTrue(taskCollection.getEventClasses().contains(PlayerJoinEvent.class));
        assertTrue(taskCollection.getMappedTasks().get(PlayerJoinEvent.class).contains(invisibleTask));
    }

    @Test
    void testAddTask_MultipleTasksSameEvent() {
        Task task1 = createTask(BlockBreakEvent.class, 3, "Break Stone");
        Task task2 = createTask(BlockBreakEvent.class, 2, "Break Dirt");
        taskCollection.addTask(task1);
        taskCollection.addTask(task2);

        assertEquals(2, taskCollection.getTaskCount());
        assertEquals(2, taskCollection.getTasks().size());
        assertEquals(1, taskCollection.getEventClasses().size());
        assertTrue(taskCollection.getEventClasses().contains(BlockBreakEvent.class));
        assertEquals(2, taskCollection.getMappedTasks().get(BlockBreakEvent.class).size());
        assertTrue(taskCollection.getMappedTasks().get(BlockBreakEvent.class).contains(task1));
        assertTrue(taskCollection.getMappedTasks().get(BlockBreakEvent.class).contains(task2));
    }

    @Test
    void testAddTask_CompositeTaskMultipleEvents() {
        Task task1 = createTask(PlayerJoinEvent.class, 1, "Join");
        Task task2 = createTask(PlayerMoveEvent.class, 1, "Move");
        TaskChoice composite = createOrComposite(task1, task2, 5, "Join or Move");
        taskCollection.addTask(composite);

        assertEquals(1, taskCollection.getTaskCount()); // Composite counts as one task
        assertEquals(1, taskCollection.getTasks().size());
        assertTrue(taskCollection.getTasks().contains(composite));
        assertEquals(2, taskCollection.getEventClasses().size()); // Listens to two events
        assertTrue(taskCollection.getEventClasses().contains(PlayerJoinEvent.class));
        assertTrue(taskCollection.getEventClasses().contains(PlayerMoveEvent.class));
        assertTrue(taskCollection.getMappedTasks().get(PlayerJoinEvent.class).contains(composite));
        assertTrue(taskCollection.getMappedTasks().get(PlayerMoveEvent.class).contains(composite));
    }

    @Test
    void testAddAllTasks_Constructor() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        Task task2 = createTask(BlockBreakEvent.class, 3, "Break");
        TaskCollection collection = new TaskCollection(List.of(task1, task2));

        assertEquals(2, collection.getTaskCount());
        assertEquals(2, collection.getTasks().size());
        assertEquals(2, collection.getEventClasses().size());
    }

    @Test
    void testAddAllTasks_Method() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        Task task2 = createTask(BlockBreakEvent.class, 3, "Break");
        taskCollection.addAllTasks(List.of(task1, task2));

        assertEquals(2, taskCollection.getTaskCount());
        assertEquals(2, taskCollection.getTasks().size());
        assertEquals(2, taskCollection.getEventClasses().size());
    }

    @Test
    void testRemoveTask_SingleEvent() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        taskCollection.addTask(task1);
        assertTrue(taskCollection.isTasksLoaded());

        taskCollection.removeTask(task1);

        assertFalse(taskCollection.isTasksLoaded());
        assertEquals(0, taskCollection.getTaskCount());
        assertTrue(taskCollection.getTasks().isEmpty());
        assertTrue(taskCollection.getEventClasses().isEmpty());
        assertTrue(taskCollection.getMappedTasks().isEmpty());
    }

    @Test
    void testRemoveTask_MultipleTasksSameEvent_Partial() {
        Task task1 = createTask(BlockBreakEvent.class, 3, "Break Stone");
        Task task2 = createTask(BlockBreakEvent.class, 2, "Break Dirt");
        taskCollection.addTask(task1);
        taskCollection.addTask(task2);
        assertEquals(2, taskCollection.getMappedTasks().get(BlockBreakEvent.class).size());

        taskCollection.removeTask(task1);

        assertTrue(taskCollection.isTasksLoaded());
        assertEquals(1, taskCollection.getTaskCount());
        assertEquals(1, taskCollection.getTasks().size());
        assertTrue(taskCollection.getTasks().contains(task2));
        assertEquals(1, taskCollection.getEventClasses().size());
        assertTrue(taskCollection.getEventClasses().contains(BlockBreakEvent.class));
        assertEquals(1, taskCollection.getMappedTasks().get(BlockBreakEvent.class).size());
        assertTrue(taskCollection.getMappedTasks().get(BlockBreakEvent.class).contains(task2));
        assertFalse(taskCollection.getMappedTasks().get(BlockBreakEvent.class).contains(task1));
    }

    @Test
    void testRemoveTask_MultipleTasksSameEvent_Full() {
        Task task1 = createTask(BlockBreakEvent.class, 3, "Break Stone");
        Task task2 = createTask(BlockBreakEvent.class, 2, "Break Dirt");
        taskCollection.addTask(task1);
        taskCollection.addTask(task2);

        taskCollection.removeTask(task1);
        taskCollection.removeTask(task2); // Remove the second task

        assertFalse(taskCollection.isTasksLoaded());
        assertEquals(0, taskCollection.getTaskCount());
        assertTrue(taskCollection.getTasks().isEmpty());
        assertTrue(taskCollection.getEventClasses().isEmpty());
        assertTrue(taskCollection.getMappedTasks().isEmpty(), "Map should be empty after removing all tasks for an event");
    }

    @Test
    void testRemoveTask_CompositeTaskMultipleEvents() {
        Task task1 = createTask(PlayerJoinEvent.class, 1, "Join");
        Task task2 = createTask(PlayerMoveEvent.class, 1, "Move");
        TaskChoice composite = createOrComposite(task1, task2, 5, "Join or Move");
        taskCollection.addTask(composite);
        assertEquals(2, taskCollection.getEventClasses().size());

        taskCollection.removeTask(composite);

        assertFalse(taskCollection.isTasksLoaded());
        assertEquals(0, taskCollection.getTaskCount());
        assertTrue(taskCollection.getTasks().isEmpty());
        assertTrue(taskCollection.getEventClasses().isEmpty());
        assertTrue(taskCollection.getMappedTasks().isEmpty());
    }

    @Test
    void testRemoveAllTasks() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        Task task2 = createTask(BlockBreakEvent.class, 3, "Break");
        Task task3 = createTask(BlockBreakEvent.class, 2, "Break Again");
        taskCollection.addAllTasks(List.of(task1, task2, task3));
        assertEquals(3, taskCollection.getTaskCount());

        taskCollection.removeAllTasks();

        assertFalse(taskCollection.isTasksLoaded());
        assertEquals(0, taskCollection.getTaskCount());
        assertTrue(taskCollection.getTasks().isEmpty());
        assertTrue(taskCollection.getEventClasses().isEmpty());
        assertTrue(taskCollection.getMappedTasks().isEmpty());
    }

    @Test
    void testSetTaskCompleted_WithRealStat_EnsuresTaskMarkedComplete() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        taskCollection.addTask(task1);

        // --- Setup for Real PlayerStat ---
        PlayerMock realPlayer = server.addPlayer("RealPlayer");
        // TeamManager setup might be needed if PlayerStat constructor requires it,
        // or if TaskCollection interacts with teams. Assuming basic setup is sufficient.
        LockoutTeam realTeam = new LockoutTeam("RealTeam", 2);
        // You might need to add the team to a TeamManager instance if logic depends on it
        PlayerStat realPlayerStat = new PlayerStat(realPlayer, realTeam);
        // --- End Setup ---

        assertFalse(task1.isCompleted());
        assertNull(task1.getScoredPlayer());

        // Call the method with the REAL PlayerStat
        taskCollection.setTaskCompleted(realPlayerStat, task1);

        // --- Assertions ---
        // Now, check if the state updated correctly using the real object
        assertTrue(task1.isCompleted(), "Task should be completed after setTaskCompleted with real PlayerStat");
        assertSame(realPlayerStat, task1.getScoredPlayer(), "Scored player should be the real PlayerStat instance");

        // Check the internal state of the real PlayerStat as well
        // Assuming PlayerStat has a method like getCompletedTasks() or similar
        // assertTrue(realPlayerStat.getCompletedTasks().contains(task1)); // Need appropriate getter
    }

    @Test
    void testSetTaskCompleted_Basic_VerifyInteraction() {
        // Arrange: Create a real Task and a mock PlayerStat
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");

        PlayerStat mockPlayerStat = Mockito.mock(PlayerStat.class);

        taskCollection.setTaskCompleted(mockPlayerStat, task1);

        // Assert: Verify the interactions occurred
        verify(mockPlayerStat, times(1)).setCompletedTask(task1);

    }

    @Test
    void testSetTaskCompleted_TimeCompletable() {
        // Task implements TimeCompletableTask
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        taskCollection.addTask(task1);
        PlayerStat mockPlayerStat = Mockito.mock(PlayerStat.class);

        assertTrue(taskCollection.getCompletedTasks().isEmpty());

        // Simulate time and location (optional for this test)
        task1.setTimeCompleted(Duration.ofSeconds(10));
        task1.setLocation(new Location(null, 1, 2, 3)); // MockBukkit world is null

        taskCollection.setTaskCompleted(mockPlayerStat, task1);

        assertEquals(1, taskCollection.getCompletedTasks().size());
        TimeCompletableTask completed = taskCollection.getCompletedTasks().peek();
        assertNotNull(completed);
        assertSame(task1, completed);
        assertEquals(Duration.ofSeconds(10), completed.getTimeCompleted());
        assertNotNull(completed.getLocation());
    }

    @Test
    void testMaxPoints() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        Task task2 = createTask(BlockBreakEvent.class, 10, "Break");
        Task task3 = createTask(PlayerMoveEvent.class, -2, "Move (Negative)"); // Negative value task
        Task task4 = createTask(PlayerMoveEvent.class, 0, "Move (Zero)"); // Zero value task
        HiddenTask invisibleTask = new HiddenTask(createTask(PlayerJoinEvent.class, 100, "Invisible"));

        taskCollection.addAllTasks(List.of(task1, task2, task3, task4, invisibleTask));

        assertEquals(15, taskCollection.maxPoints(), "Max points should sum only positive values from non-invisible tasks");
    }

    @Test
    void testRemainingPoints_Initial() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        Task task2 = createTask(BlockBreakEvent.class, 10, "Break");
        Task task3 = createTask(PlayerMoveEvent.class, -2, "Move (Negative)");
        HiddenTask invisibleTask = new HiddenTask(createTask(PlayerJoinEvent.class, 100, "Invisible"));
        taskCollection.addAllTasks(List.of(task1, task2, task3, invisibleTask));

        assertEquals(15, taskCollection.remainingPoints(), "Initially, remaining points should equal max points (positive only)");
    }

    @Test
    void testRemainingPoints_AfterCompletion() {
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        Task task2 = createTask(BlockBreakEvent.class, 10, "Break");
        Task task3 = createTask(PlayerMoveEvent.class, -2, "Move (Negative)");
        HiddenTask invisibleTask = new HiddenTask(createTask(PlayerJoinEvent.class, 100, "Invisible"));
        taskCollection.addAllTasks(List.of(task1, task2, task3, invisibleTask));

        PlayerStat mockPlayerStat = Mockito.mock(PlayerStat.class);
        taskCollection.setTaskCompleted(mockPlayerStat, task1); // Complete task1 (value 5)

        assertEquals(10, taskCollection.remainingPoints(), "Remaining points should decrease by completed task's positive value");

        taskCollection.setTaskCompleted(mockPlayerStat, task3); // Complete task3 (value -2)
        assertEquals(10, taskCollection.remainingPoints(), "Completing a negative value task should not change remaining points");

        taskCollection.setTaskCompleted(mockPlayerStat, task2); // Complete task2 (value 10)
        assertEquals(0, taskCollection.remainingPoints(), "Remaining points should be 0 after all positive tasks completed");
    }

    @Test
    void testIsTasksLoaded() {
        assertFalse(taskCollection.isTasksLoaded());
        Task task1 = createTask(PlayerJoinEvent.class, 5, "Join");
        taskCollection.addTask(task1);
        assertTrue(taskCollection.isTasksLoaded());
        taskCollection.removeTask(task1);
        assertFalse(taskCollection.isTasksLoaded());
    }
}
