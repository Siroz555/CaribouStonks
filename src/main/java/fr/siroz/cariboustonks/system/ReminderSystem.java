package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.reminder.TimedObject;
import fr.siroz.cariboustonks.core.service.json.JsonFileService;
import fr.siroz.cariboustonks.core.service.json.JsonProcessingException;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.system.System;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.util.DeveloperTools;
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
public final class ReminderSystem implements System {

	private static final Path REMINDER_PATH = CaribouStonks.CONFIG_DIR.resolve("reminder.json");
	private static final int MONITOR_INTERVAL_SECONDS = 10;

	private final PriorityQueue<TimedObject> queue = new PriorityQueue<>(Comparator.comparing(TimedObject::expirationTime));
	private final Map<String, TimedObject> activeReminders = new ConcurrentHashMap<>();
	private final Set<String> preNotifiedIds = ConcurrentHashMap.newKeySet();
	private final Map<String, ReminderComponent> registeredComponents = new ConcurrentHashMap<>();

	private final ScheduledExecutorService scheduler;
	private volatile boolean loaded = false;

	@ApiStatus.Internal
	public ReminderSystem() {
		this.scheduler = Executors.newScheduledThreadPool(1, r -> {
			Thread thread = new Thread(r, "ReminderSystem-Monitor");
			thread.setDaemon(true);
			return thread;
		});
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
		feature.getComponent(ReminderComponent.class)
				.ifPresent(component -> registerComponent(feature, component));
	}

	private void registerComponent(Feature feature, ReminderComponent component) {
		String reminderType = component.getReminderType();

		if (registeredComponents.containsKey(reminderType)) {
			CaribouStonks.LOGGER.warn(
					"[ReminderSystem] Reminder type '{}' is already registered, skipping feature '{}'",
					reminderType,
					feature.getShortName()
			);
			return;
		}

		registeredComponents.put(reminderType, component);

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info(
					"[ReminderSystem] Registered reminder type '{}' from feature '{}'",
					reminderType,
					feature.getShortName()
			);
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
		if (!registeredComponents.containsKey(obj.type())) {
			throw new IllegalArgumentException("No reminder registered for type: " + obj.type());
		}

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

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[ReminderSystem] Added object: {} (type: {}, expires: {})", objectId, obj.type(), obj.expirationTime());
		}
	}

	/**
	 * Retrieves a list of reminders paired with their associated {@link TimedObject} instances.
	 * Each entry in the list represents a {@link ReminderComponent} object and a corresponding {@link TimedObject}
	 * whose type matches the reminder.
	 *
	 * @return a {@link List} of {@link Pair} objects, where each {@link Pair} contains a {@link ReminderComponent}
	 * and a {@link TimedObject} whose types are associated.
	 */
	public @NotNull List<Pair<ReminderComponent, TimedObject>> getReminders() {
		List<Pair<ReminderComponent, TimedObject>> result = new ArrayList<>();

		synchronized (queue) {
			for (TimedObject obj : queue) {
				ReminderComponent component = registeredComponents.get(obj.type());
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
		loadTimedObjects()
				.thenAccept(list ->
						TickScheduler.getInstance().runLater(() -> loadExistingObjects(list), 2, TimeUnit.SECONDS)
				).exceptionally(ex -> {
					CaribouStonks.LOGGER.error("[ReminderSystem] Failed to load reminders", ex);
					return null;
				});
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
			JsonFileService.get().save(REMINDER_PATH, objectsToSave);
			CaribouStonks.LOGGER.info("[ReminderSystem] Saved {} active reminders to disk", objectsToSave.size());
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[ReminderSystem] Failed to save reminders", ex);
		}

		try {
			scheduler.shutdown();
		} catch (Exception ignored) {
		}
	}

	private @NotNull CompletableFuture<List<TimedObject>> loadTimedObjects() {
		if (!Files.exists(REMINDER_PATH)) {
			return CompletableFuture.completedFuture(List.of());
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				List<TimedObject> loaded = JsonFileService.get().loadList(REMINDER_PATH, TimedObject.class);

				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.info("[ReminderSystem] Loaded {} reminders from disk", loaded.size());
				}

				return loaded;
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[ReminderSystem] Unable to load timedObject list", ex);
				return Collections.emptyList();
			}
		});
	}

	private void loadExistingObjects(@NotNull List<TimedObject> loadedObjects) {
		if (loadedObjects.isEmpty()) {
			return;
		}

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

	private boolean shouldProcessPreNotification(@NotNull TimedObject obj, @NotNull Instant now) {
		if (!preNotifiedIds.add(obj.id())) {
			return false;
		}

		ReminderComponent component = registeredComponents.get(obj.type());
		if (component == null) {
			preNotifiedIds.remove(obj.id());
			return false;
		}

		Optional<Duration> preNotifyDuration = component.getPreNotifyDuration();
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

	private void processPreNotification(@NotNull TimedObject obj) {
		ReminderComponent component = registeredComponents.get(obj.type());
		if (component != null) {
			try {
				component.handlePreExpiration(obj);

				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.info("[ReminderSystem] Pre-notification triggered for: {}", obj.id());
				}
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ReminderSystem] Error in pre-notification handler for: {}", obj.id(), ex);
			}
		}
	}

	private void onExpire(@NotNull TimedObject obj) {
		ReminderComponent component = registeredComponents.get(obj.type());
		if (component != null) {
			try {
				component.handleExpiration(obj);

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
}
