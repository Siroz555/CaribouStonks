package fr.siroz.cariboustonks.manager.reminder;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.json.JsonProcessingException;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.Manager;
import it.unimi.dsi.fastutil.Pair;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

/**
 * Manages timed reminders by storing, processing, and serializing {@link TimedObject} instances.
 * This manager ensures that expired objects are handled correctly and provides functionality
 * to add, remove, retrieve, and persist reminders across player sessions.
 */
public final class ReminderManager implements Manager {

	private static final Path REMAINDER_PATH = CaribouStonks.CONFIG_DIR.resolve("reminder.json");
	private static final int MONITOR_INTERVAL_SECONDS = 10;
	private static final int AUTO_SAVE_INTERVAL_SECONDS = 60;

	private final PriorityQueue<TimedObject> queue = new PriorityQueue<>(Comparator.comparing(TimedObject::expirationTime));
	private final Map<String, TimedObject> activeReminders = new ConcurrentHashMap<>();
	private final Set<String> preNotifiedIds = ConcurrentHashMap.newKeySet();
	private final Map<String, Reminder> registeredComponents = new ConcurrentHashMap<>();

	private final ScheduledExecutorService scheduler;
	private volatile boolean loaded = false;
	private volatile boolean isDirty = false;
	private volatile int lastSavedHash = 0;

	@ApiStatus.Internal
	public ReminderManager() {
		this.scheduler = Executors.newScheduledThreadPool(1, r -> {
			Thread thread = new Thread(r, "ReminderSystem-Monitor");
			thread.setDaemon(true);
			return thread;
		});
		SkyBlockEvents.JOIN.register(_serverName -> this.onSkyBlockJoin());
		this.startMonitoring();
		this.startAutoSave();
	}

	/**
	 * Registers a reminder for the specified feature.
	 *
	 * @param feature the feature class
	 */
	@Override
	public void register(@NotNull Feature feature) {
		if (feature instanceof Reminder reminder) {
			registeredComponents.put(reminder.reminderType(), reminder);
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
		String objectId = obj.id();

		if (activeReminders.containsKey(objectId)) {
			if (!replaceIfExists) {
				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.debug("[ReminderSystem] Skipped duplicate object: {}", objectId);
				}
				return;
			}

			// Remove existing object
			synchronized (queue) {
				queue.removeIf(o -> o.id().equals(objectId));
			}
			activeReminders.remove(objectId);
			preNotifiedIds.remove(objectId);

			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.info("[ReminderSystem] Replaced object: {}", objectId);
			}
		}

		synchronized (queue) {
			queue.add(obj);
		}
		activeReminders.put(objectId, obj);
		isDirty = true;

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[ReminderSystem] Added object: {} (type: {}, expires: {})", objectId, obj.type(), obj.expirationTime());
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
		List<Pair<Reminder, TimedObject>> result = new ArrayList<>();

		synchronized (queue) {
			for (TimedObject obj : queue) {
				Reminder component = registeredComponents.get(obj.type());
				if (component != null) {
					result.add(Pair.of(component, obj));
				}
			}
		}

		result.sort(Comparator.comparing(pair -> pair.right().expirationTime()));
		return result;
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

		List<TimedObject> objectsToSave;
		synchronized (queue) {
			objectsToSave = new ArrayList<>(queue);
		}

		try {
			CaribouStonks.core().getJsonFileService().save(REMAINDER_PATH, objectsToSave);
			CaribouStonks.LOGGER.info("[ReminderSystem] Saved {} active reminders to disk", objectsToSave.size());
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[ReminderSystem] Failed to save reminders", ex);
		}

		try {
			scheduler.shutdown();
		} catch (Exception ignored) {
		}
	}

	private void startMonitoring() {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				processReminders();
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ReminderSystem] Error in monitoring task", ex);
			}
		}, MONITOR_INTERVAL_SECONDS, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[ReminderSystem] Started monitoring with {}s interval", MONITOR_INTERVAL_SECONDS);
		}
	}

	private void startAutoSave() {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				autoSave();
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ReminderSystem] Error in auto-save task", ex);
			}
		}, AUTO_SAVE_INTERVAL_SECONDS, AUTO_SAVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	private @NotNull CompletableFuture<List<TimedObject>> loadTimedObjects() {
		if (!Files.exists(REMAINDER_PATH)) {
			return CompletableFuture.completedFuture(List.of());
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				return CaribouStonks.core().getJsonFileService().loadList(REMAINDER_PATH, TimedObject.class);
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[ReminderManager] Unable to load timedObject list", ex);
				return Collections.emptyList();
			}
		});
	}

	private void loadExistingObjects(@NotNull List<TimedObject> loadedObjects) {
		if (loadedObjects.isEmpty()) return;

		Instant now = Instant.now();
		int expired = 0;
		int loaded = 0;
		int orphaned = 0;

		for (TimedObject obj : loadedObjects) {
			if (!registeredComponents.containsKey(obj.type())) {
				orphaned++;
				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.warn("[ReminderSystem] Orphaned reminder '{}' of type '{}' - no component registered", obj.id(), obj.type());
				}
				continue;
			}

			if (obj.expirationTime().isBefore(now)) {
				expired++;
				onExpire(obj);
			} else {
				loaded++;
				synchronized (queue) {
					queue.add(obj);
				}
				activeReminders.put(obj.id(), obj);
			}
		}

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[ReminderSystem] Processed saved reminders: {} loaded, {} expired, {} orphaned", loaded, expired, orphaned);
		}
	}

	private void processReminders() {
		Instant now = Instant.now();
		List<TimedObject> expiredObjects = new ArrayList<>();

		synchronized (queue) {
			// Handle pre-notifications
			queue.stream()
					.filter(obj -> shouldProcessPreNotification(obj, now))
					.forEach(this::processPreNotification);

			// Collect expired objects
			while (!queue.isEmpty() && !queue.peek().expirationTime().isAfter(now)) {
				expiredObjects.add(queue.poll());
			}
		}

		// Handle expirations
		expiredObjects.forEach(obj -> {
			activeReminders.remove(obj.id());
			preNotifiedIds.remove(obj.id());
			onExpire(obj);
		});
	}

	private boolean shouldProcessPreNotification(TimedObject obj, Instant now) {
		if (!preNotifiedIds.add(obj.id())) return false;

		Reminder component = registeredComponents.get(obj.type());
		if (component == null) {
			preNotifiedIds.remove(obj.id());
			return false;
		}

		Optional<Duration> preNotifyDuration = component.preNotifyDuration();
		if (preNotifyDuration.isEmpty()) {
			preNotifiedIds.remove(obj.id());
			return false;
		}

		Instant preNotifyTime = obj.expirationTime().minus(preNotifyDuration.get());

		if (now.isBefore(preNotifyTime)) {
			preNotifiedIds.remove(obj.id());
			return false;
		}

		return true;
	}

	private void processPreNotification(TimedObject obj) {
		Reminder component = registeredComponents.get(obj.type());
		if (component != null) {
			try {
				component.onPreExpire(obj);

				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.info("[ReminderSystem] Pre-notification triggered for: {}", obj.id());
				}
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ReminderSystem] Error in pre-notification handler for: {}", obj.id(), ex);
			}
		}
	}

	private void onExpire(TimedObject obj) {
		Reminder component = registeredComponents.get(obj.type());
		if (component != null) {
			try {
				component.onExpire(obj);

				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.info("[ReminderSystem] Reminder expired: {} (type: {})", obj.id(), obj.type());
				}
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ReminderSystem] Error in expiration handler for: {}", obj.id(), ex);
			}
		} else {
			CaribouStonks.LOGGER.warn("[ReminderSystem] No component found for expired reminder: {} (type: {})", obj.id(), obj.type());
		}
	}

	private void autoSave() {
		if (!loaded) return;

		if (!isDirty) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.info("[ReminderSystem] Auto-save skipped: no changes (not dirty)");
			}
			return;
		}

		if (saveIfChanged()) {
			isDirty = false;
		}
	}

	private boolean saveIfChanged() {
		List<TimedObject> objectsToSave;
		synchronized (queue) {
			objectsToSave = new ArrayList<>(queue);
		}

		// If empty and was empty before, skip
		if (objectsToSave.isEmpty() && lastSavedHash == 0) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.info("[ReminderSystem] Save skipped: queue is empty");
			}
			return false;
		}

		int currentHash = calculateHash(objectsToSave);
		if (currentHash == lastSavedHash) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.info("[ReminderSystem] Save skipped: content unchanged (hash: {})", currentHash);
			}
			return false;
		}

		try {
			CaribouStonks.core().getJsonFileService().save(REMAINDER_PATH, objectsToSave);
			lastSavedHash = currentHash;

			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.info(
						"[ReminderSystem] Saved {} reminders (hash: {})",
						objectsToSave.size(),
						currentHash
				);
			}

			return true;
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[ReminderSystem] Failed to save reminders", ex);
			return false;
		}
	}

	private int calculateHash(List<TimedObject> objects) {
		if (objects.isEmpty()) return 0;

		// Il faut que ça soit consistent
		List<TimedObject> sorted = new ArrayList<>(objects);
		sorted.sort(Comparator.comparing(TimedObject::id));

		int hash = 1;
		for (TimedObject obj : sorted) {
			hash = 31 * hash + obj.id().hashCode();
			hash = 31 * hash + obj.expirationTime().hashCode();
		}

		return hash;
	}
}
