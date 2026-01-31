package fr.siroz.cariboustonks.system.keybinds;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.crash.CrashType;
import fr.siroz.cariboustonks.event.CustomScreenEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.System;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The {@code KeyBindSystem} manages the registration and handling of {@link KeyBind} for features,
 * providing functionality to trigger actions based on key presses, manage KeyBind states,
 * and integrate KeyBind with custom screens.
 * <p>
 * The registration of KeyBinds is done via the {@link KeyBindingHelper} from Fabric API.
 * <p>
 * This allows for better management of KeyBind states compared to the Fabric API and provides
 * improved tracking of the different uses of each KeyBind within the Mod.
 */
public final class KeyBindSystem implements System {

	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(CaribouStonks.identifier("mod"));

	private final Set<KeyBind> enabledKeyBinds = ConcurrentHashMap.newKeySet();
	private final Map<Feature, List<KeyBind>> keyBinds = new ConcurrentHashMap<>();

	@ApiStatus.Internal
	public KeyBindSystem() {
		ClientTickEvents.END_CLIENT_TICK.register(_client -> this.triggerKeyBinds());
		CustomScreenEvents.KEY_PRESSED.register(this::handleKeyPressed);
	}

	@Override
	public void register(@NotNull Feature feature) {
		feature.getComponent(KeyBindComponent.class)
				.ifPresent(keyBindComponent -> register(feature, keyBindComponent));
	}

	@EventHandler(event = "CustomScreenEvents.KEY_PRESSED")
	private void handleKeyPressed(Screen screen, KeyEvent input, @NotNull Slot slot) {
		triggerKeyBindsInScreen(screen, input, slot);
	}

	private void register(Feature feature, KeyBindComponent keyBindComponent) {
		try {
			keyBinds.putIfAbsent(feature, new LinkedList<>());
			keyBinds.get(feature).addAll(keyBindComponent.keyBinds());
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error(
					"[KeyBindManager] Failed to register KeyBinds in {}", feature.getClass().getName(), ex);
		}

		enableFeatureKeyBinds(feature);
	}

	/**
	 * Enables and registers all key binds associated with the given feature.
	 */
	private void enableFeatureKeyBinds(@NotNull Feature feature) {
		if (!keyBinds.containsKey(feature)) {
			return;
		}

		for (KeyBind keyBind : keyBinds.get(feature)) {
			registerKeyBind(keyBind);
		}
	}

	/**
	 * Registers a {@link KeyBind} into the {@link KeyBindingHelper} Fabric API, and adds it for handling key presses.
	 * Ensures that key binds with duplicate names are not added.
	 */
	private void registerKeyBind(KeyBind keyBind) {
		if (enabledKeyBinds.stream().anyMatch(k -> k.getName().equals(keyBind.getName()))) {
			throw new IllegalStateException("Unable to add KeyBind '" + keyBind.getName() + "' name already exists");
		}

		enabledKeyBinds.add(keyBind);
		KeyBindingHelper.registerKeyBinding(keyBind.getKeyBinding());
	}

	/**
	 * Triggers the registered key binds by iterating over them and evaluating
	 * whether their associated conditions for activation are met. This includes
	 * handling "first press" behavior, where certain key binds should only
	 * trigger once per key press cycle.
	 */
	@EventHandler(event = "ClientTickEvents.END_CLIENT_TICK")
	private void triggerKeyBinds() {
		checkAllKeyBinds(keyBind -> {
			if (keyBind.isFirstPress()) {
				if (keyBind.getKeyBinding().isDown() && !keyBind.isPressed()) {
					keyBind.onPress();
				}

				keyBind.setPressed(keyBind.getKeyBinding().isDown());
			} else if (keyBind.getKeyBinding().isDown()) {
				keyBind.onPress();
			}
		});
	}

	/**
	 * Iterates through all registered key binds associated with various features and processes each
	 * key bind that does not have a screen press handler.
	 */
	private void checkAllKeyBinds(Consumer<KeyBind> checkKeyBind) {
		for (Feature feature : keyBinds.keySet()) {
			for (KeyBind keyBind : keyBinds.get(feature)) {
				if (keyBind.hasScreenPressHandler()) {
					continue;
				}

				try {
					checkKeyBind.accept(keyBind);
				} catch (Throwable throwable) {
					CaribouStonks.core().getCrashManager().reportCrash(
							CrashType.KEYBINDING,
							keyBind.getName(),
							feature.getClass().getName() + "." + keyBind.getName(),
							"handling",
							throwable);
				}
			}
		}
	}

	/**
	 * Handles triggering key bind actions within a specific screen context.
	 * <p>
	 * This method is different from {@link #checkAllKeyBinds(Consumer)}, because it's not possible to match
	 * the 'firstPress' behavior and the connections with the actual {@link net.minecraft.client.KeyMapping}.
	 */
	private void triggerKeyBindsInScreen(Screen screen, KeyEvent input, Slot slot) {
		for (Feature feature : keyBinds.keySet()) {
			for (KeyBind keyBind : keyBinds.get(feature)) {
				try {
					if (keyBind.hasScreenPressHandler() && keyBind.getKeyBinding().matches(input)) {
						keyBind.onScreenPress(screen, slot);
					}
				} catch (Throwable throwable) {
					CaribouStonks.core().getCrashManager().reportCrash(
							CrashType.KEYBINDING,
							keyBind.getName(),
							feature.getClass().getName() + "." + keyBind.getName(),
							"handling in screen",
							throwable);
				}
			}
		}
	}
}
