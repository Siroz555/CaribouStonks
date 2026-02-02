package fr.siroz.cariboustonks.feature.keyshortcut;

import com.google.common.reflect.TypeToken;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.json.JsonProcessingException;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.screen.keyshortcut.KeyShortcutScreen;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class KeyShortcutFeature extends Feature {

	private static final Path SHORTCUTS_PATH = CaribouStonks.CONFIG_DIR.resolve("shortcuts.json");

	private final Map<String, KeyShortcut> shortcuts = new ConcurrentHashMap<>();
	private long lastKeyPressed = 0;

	public KeyShortcutFeature() {
		ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
		ClientLifecycleEvents.CLIENT_STOPPING.register(this::saveShortcuts);
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("keyShortcuts")
						.executes(StonksUtils.openScreen(() -> KeyShortcutScreen.create(null))))
		));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && !shortcuts.isEmpty();
	}

	public Map<String, KeyShortcut> getShortcutsCopy() {
		return Map.copyOf(shortcuts);
	}

	public void updateShortcuts(@NotNull Map<String, KeyShortcut> newShortcuts) {
		shortcuts.clear();
		shortcuts.putAll(newShortcuts);
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STOPPING") // + KeyShortcutScreen#saveShortcuts
	public void saveShortcuts(MinecraftClient client) {
		Map<String, Integer> shortcutsToSave = new HashMap<>();
		// Jcp pk, j'ai mis un record pour gérer en interne les shortcuts,
		// mais c'est beaucoup plus agréable à gérer dans le Screen -_-
		for (Map.Entry<String, KeyShortcut> entry : shortcuts.entrySet()) {
			shortcutsToSave.put(entry.getKey(), entry.getValue().keyCode());
		}

		try {
			CaribouStonks.core().getJsonFileService().save(SHORTCUTS_PATH, shortcutsToSave);
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[KeyShortcuts] Unable to save shortcuts", ex);
		}
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STARTED")
	private void onClientStarted(MinecraftClient client) {
		loadKeyShortcuts().thenAccept(this::loadExistingKeyShortcuts);
	}

	private CompletableFuture<Map<String, Integer>> loadKeyShortcuts() {
		if (!Files.exists(SHORTCUTS_PATH)) {
			return CompletableFuture.completedFuture(Map.of());
		}

		return CompletableFuture.supplyAsync(() -> {
			Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
			try {
				return CaribouStonks.core().getJsonFileService().loadMap(SHORTCUTS_PATH, mapType);
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[KeyShortcuts] Unable to load shortcuts", ex);
				return Collections.emptyMap();
			}
		});
	}

	private void loadExistingKeyShortcuts(@NotNull Map<String, Integer> shortcutsMap) {
		int loaded = 0;
		for (Map.Entry<String, Integer> entry : shortcutsMap.entrySet()) {
			if (entry.getKey() == null || entry.getKey().isBlank()) {
				continue;
			}

			shortcuts.put(entry.getKey(), new KeyShortcut(entry.getKey(), entry.getValue()));
			loaded++;
		}

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[KeyShortcuts] Loaded {} KeyShortcut", loaded);
		}
	}

	@EventHandler(event = "ClientTickEvents.END_CLIENT_TICK")
	private void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null) return;
		if (client.currentScreen != null) return;
		if (!isEnabled()) return;
		if (System.currentTimeMillis() - lastKeyPressed < ConfigManager.getConfig().general.keyShortcutCooldown) return;

		for (KeyShortcut shortcut : shortcuts.values()) {
			if (shortcut.command().isBlank() || shortcut.keyCode() == -1) {
				continue;
			}

			boolean pressed;
			if (shortcut.keyCode() <= -2000) {
				int mouseButton = -2000 - shortcut.keyCode();
				pressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), mouseButton) == GLFW.GLFW_PRESS;
			} else {
				pressed = InputUtil.isKeyPressed(client.getWindow(), shortcut.keyCode());
			}

			if (pressed) {
				lastKeyPressed = System.currentTimeMillis();
				handleShortcut(client, shortcut);
			}
		}
	}

	private void handleShortcut(@NotNull MinecraftClient client, @NotNull KeyShortcut shortcut) {
		if (client.player != null && client.player.networkHandler != null) {
			String shortcutCommand = shortcut.command();
			String command = shortcutCommand.startsWith("/") ? shortcutCommand.substring(1) : shortcutCommand;
			client.player.networkHandler.sendChatCommand(command);
		}
	}
}
