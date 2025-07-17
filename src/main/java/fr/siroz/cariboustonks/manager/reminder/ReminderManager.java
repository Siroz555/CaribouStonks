package fr.siroz.cariboustonks.manager.reminder;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.Manager;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Manages timed reminders by storing, processing, and serializing {@link TimedObject} instances.
 * This manager ensures that expired objects are handled correctly and provides functionality
 * to add, remove, retrieve, and persist reminders across player sessions.
 */
public final class ReminderManager implements Manager {

    private static final Path REMAINDER_PATH = CaribouStonks.CONFIG_DIR.resolve("reminder.json");
    //private final ObjectHeapPriorityQueue<TimedObject> queue = new ObjectHeapPriorityQueue<>(Comparator.comparing(TimedObject::getExpirationTime));
    private final PriorityQueue<TimedObject> queue = new PriorityQueue<>(Comparator.comparing(TimedObject::expirationTime));
    private final Set<String> preNotifiedSet = ConcurrentHashMap.newKeySet();
    private final Map<String, TimedObject> objectSet = new ConcurrentHashMap<>();
    private final Object2ObjectMap<Feature, Reminder> reminders = new Object2ObjectOpenHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean loaded = false;

    @ApiStatus.Internal
    public ReminderManager() {
        SkyBlockEvents.JOIN.register(_serverName -> this.onSkyBlockJoin());
        startMonitoring();
    }

    /**
     * Registers a reminder for the specified feature.
     *
     * @param feature the feature class
     */
    @Override
    public void register(@NotNull Feature feature) {
        if (feature instanceof Reminder reminder) {
            reminders.put(feature, reminder);
        }
    }

    /**
     * Adds a {@link TimedObject} to the queue.
     *
     * @param obj the {@link TimedObject} to add
     * @see #addTimedObject(TimedObject, boolean)
     */
    public void addTimedObject(@NotNull TimedObject obj) {
        addTimedObject(obj, false);
    }

    /**
     * Adds a {@link TimedObject} to the queue.
     * If the object already exists, it will be added only if {@code replaceIfExists} is {@code true}.
     *
     * @param obj             the {@link TimedObject} to add
     * @param replaceIfExists if {@code true}, replaces an existing object with the same ID
     *                        if {@code false}, the object is added only if it does not already exist
     * @see #addTimedObject(TimedObject)
     */
    public void addTimedObject(@NotNull TimedObject obj, boolean replaceIfExists) {
		if (objectSet.containsKey(obj.id())) {
            if (replaceIfExists) {
                queue.removeIf(o -> o.id().equals(obj.id()));
				objectSet.remove(obj.id());
            } else {
                return;
            }
        }

        queue.add(obj);
        objectSet.put(obj.id(), obj);
        if (DeveloperTools.isInDevelopment()) {
            if (replaceIfExists) {
                CaribouStonks.LOGGER.info("[ReminderManager] Updated {}", obj.id());
            } else {
                CaribouStonks.LOGGER.info("[ReminderManager] Added {}", obj.id());
            }
        }
    }

    /**
     * Retrieves a list of reminders paired with their associated {@link TimedObject} instances.
     * Each entry in the list represents a {@link Reminder} object and a corresponding {@link TimedObject}
     * whose type matches the reminder.
     *
     * @return a {@link List} of {@link Pair} objects, where each {@link Pair} contains a {@link Reminder}
     * and a {@link TimedObject} whose types are associated.
     */
    public @NotNull List<Pair<Reminder, TimedObject>> getReminders() {
        List<Pair<Reminder, TimedObject>> reminderList = new ArrayList<>();
        for (TimedObject obj : queue) {
            reminders.values().stream()
                    .filter(v -> v.reminderType().equals(obj.type()))
                    .map(v -> Pair.of(v, obj))
                    .findFirst()
                    .ifPresent(reminderList::add);
        }

        reminderList.sort(Comparator.comparing(pair -> pair.right().expirationTime()));

        return reminderList;
    }

    private void onSkyBlockJoin() {
        if (loaded) {
			return;
		}

        loaded = true;
        loadTimedObjects().thenAccept(timedObjects -> TickScheduler.getInstance().runLater(
				() -> loadExistingObjects(timedObjects), 2, TimeUnit.SECONDS));
    }

    @Override
    public void onShutdown() {
        if (!loaded) {
			return;
		}

        loaded = false;

        List<TimedObject> objectsList = !queue.isEmpty() ? new ArrayList<>(queue) : List.of();
        CaribouStonks.core().getJsonFileService().save(REMAINDER_PATH, objectsList);
    }

    private @NotNull CompletableFuture<List<TimedObject>> loadTimedObjects() {
        if (!Files.exists(REMAINDER_PATH)) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(
                () -> CaribouStonks.core().getJsonFileService().loadList(REMAINDER_PATH, TimedObject.class));
    }

    private void loadExistingObjects(@NotNull List<TimedObject> loadedObjects) {
        CaribouStonks.LOGGER.info("[Reminder] Loading {} TimedObject", loadedObjects.size());
        Instant now = Instant.now();
        for (TimedObject obj : loadedObjects) {
            if (obj.expirationTime().isBefore(now)) {
                onExpire(obj);
            } else {
                queue.add(obj);
            }
        }
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            Instant now = Instant.now();
            List<TimedObject> expiredObjects = new ArrayList<>();

            synchronized (queue) {

                queue.stream()
                        .map(obj -> getReminder(obj).map(r -> Map.entry(obj, r)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(entry -> entry.getValue().preNotifyDuration().isPresent())
                        .filter(entry -> preNotifiedSet.add(entry.getKey().id()))
                        .forEach(entry -> {
                            TimedObject obj = entry.getKey();
                            Reminder reminder = entry.getValue();
                            if (reminder.preNotifyDuration().isPresent()) { // Intellij Warn -_-
                                Instant preNotifyTime = obj.expirationTime().minus(reminder.preNotifyDuration().get());
                                if (!now.isBefore(preNotifyTime)) {
                                    reminder.onPreExpire(obj);
                                } else {
                                    preNotifiedSet.remove(obj.id());
                                }
                            }
                        });

                while (!queue.isEmpty() && queue.peek().expirationTime().isBefore(now)) {
                    expiredObjects.add(queue.poll());
                    //expiredObjects.add(queue.dequeue());
                }
            }

            expiredObjects.forEach(obj -> {
                objectSet.remove(obj.id());
                preNotifiedSet.remove(obj.id());
                onExpire(obj);
            });

        }, 10, 10, TimeUnit.SECONDS);
    }

    private @NotNull Optional<Reminder> getReminder(@NotNull TimedObject timedObject) {
        return reminders.values().stream()
                .filter(r -> r.reminderType().equals(timedObject.type()))
                .findFirst();
    }

    private void onExpire(@NotNull TimedObject timedObject) {
        reminders.values().stream()
                .filter(reminder -> reminder.reminderType().equals(timedObject.type()))
                .findFirst()
                .ifPresent(reminder -> reminder.onExpire(timedObject));
    }
}
