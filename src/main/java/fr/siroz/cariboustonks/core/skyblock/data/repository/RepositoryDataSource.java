package fr.siroz.cariboustonks.core.skyblock.data.repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.infrastructure.json.JsonFileService;
import fr.siroz.cariboustonks.core.infrastructure.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockEnchantment;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockItemRegistry;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class RepositoryDataSource {
	private static final String ENCHANTMENTS_FILE = "enchantments.json";
	private static final String ATTRIBUTES_FILE = "attributes.json";
	private static final String REFORGES_FILE = "reforges.json";
	private static final String MATERIALS_FILE = "legacy_hypixel_material.json";

	private final ExecutorService executor;
	private final boolean devMode;
	private final RemoteRepository repository;

	public RepositoryDataSource() {
		this.executor = AsyncScheduler.getInstance().blockingExecutor();
		this.devMode = DeveloperTools.isInDevelopment();
		this.repository = new RemoteRepository(this.executor, this.devMode);

		SkyBlockEvents.JOIN_EVENT.register((_) -> this.onSkyBlockJoin());

		CompletableFuture.runAsync(this::loadRepositoryData, this.executor) // dernier état connu
				.thenCompose(_ -> this.repository.getReadyFuture())
				.thenAcceptAsync(ready -> {
					if (ready) this.loadRepositoryData(); // données fraîches
					else CaribouStonks.LOGGER.warn("[RepositoryDataSource] Sync failed, using local files only");
				}, this.executor).exceptionally(throwable -> {
					CaribouStonks.LOGGER.error("[RepositoryDataSource] Unexpected error during initial load", throwable);
					return null;
				});
	}

	private void onSkyBlockJoin() {
		if (repository.getReadyFuture().isDone() && !repository.isReady() && !repository.isFetchInProgress()) {
			PlayerContext.sendMessageWithPrefix(Component.empty()
					.append(Component.literal("⚠ Repository data could not be loaded. ").withStyle(ChatFormatting.RED))
					.append(Component.literal("Some features may be unavailable. ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("Use ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("/cariboustonks reload repository").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal(" to retry.").withStyle(ChatFormatting.GRAY)));
		}
	}

	private void loadRepositoryData() {
		loadRepositoryFile(ENCHANTMENTS_FILE, "enchantments", JsonArray.class, this::applyEnchantments);
		loadRepositoryFile(ATTRIBUTES_FILE, "attributes", JsonArray.class, this::applyAttributes);
		loadRepositoryFile(REFORGES_FILE, "attributes", JsonArray.class, this::applyReforges);
		loadRepositoryFile(MATERIALS_FILE, "hypixelMaterial", JsonObject.class, this::applyLegacyHypixelMaterial);

		CaribouStonks.LOGGER.info("[RepositoryDataSource] Loaded {} enchantments", SkyBlockItemRegistry.sizeOfEnchantments());
		CaribouStonks.LOGGER.info("[RepositoryDataSource] Loaded {} attributes", SkyBlockItemRegistry.sizeOfAttributes());
		CaribouStonks.LOGGER.info("[RepositoryDataSource] Loaded {} reforges", SkyBlockItemRegistry.sizeOfReforges());
		CaribouStonks.LOGGER.info("[RepositoryDataSource] Loaded {} legacy Hypixel material", SkyBlockItemRegistry.sizeOfLegacyHypixelMaterial());
	}

	private <R> void loadRepositoryFile(String fileName, String label, Class<R> rawType, Consumer<R> handler) {
		try {
			Path path = repository.resolveDataFile(fileName);
			R raw = JsonFileService.get().load(path, rawType);
			if (raw == null) {
				if (devMode) CaribouStonks.LOGGER.warn("[RepositoryDataSource] Unable to load {}, content is null. (File not exists)", label);
				return;
			}
			handler.accept(raw);
		} catch (Exception ex) {
			if (devMode) CaribouStonks.LOGGER.error("[RepositoryDataSource] There was an error while loading {}", label, ex);
		}
	}

	private void applyEnchantments(@NonNull JsonArray jsonArray) {
		if (jsonArray.isEmpty()) return;

		List<SkyBlockEnchantment> enchantments = new ArrayList<>(jsonArray.size());
		for (JsonElement element : jsonArray) {
			JsonObject json = element.getAsJsonObject();
			SkyBlockEnchantment enchantment = new SkyBlockEnchantment(
					json.get("id").getAsString(),
					json.get("name").getAsString(),
					json.get("maxLevel").getAsInt(),
					json.has("goodLevel")
							? OptionalInt.of(json.get("goodLevel").getAsInt()) : OptionalInt.empty()
			);
			enchantments.add(enchantment);
		}
		SkyBlockItemRegistry.loadEnchantments(enchantments);
	}

	private void applyAttributes(@NonNull JsonArray jsonArray) {
		if (jsonArray.isEmpty()) return;

		List<SkyBlockAttribute> attributes = new ArrayList<>(jsonArray.size());
		for (JsonElement element : jsonArray) {
			JsonObject json = element.getAsJsonObject();
			SkyBlockAttribute attribute = new SkyBlockAttribute(
					json.get("name").getAsString(),
					json.get("shardName").getAsString(),
					json.get("id").getAsString(),
					json.get("skyBlockApiId").getAsString()
			);
			attributes.add(attribute);
		}
		SkyBlockItemRegistry.loadAttributes(attributes);
	}

	private void applyReforges(@NonNull JsonArray jsonArray) {
		if (jsonArray.isEmpty()) return;

		Map<String, String> reforges = new HashMap<>(jsonArray.size());
		for (JsonElement element : jsonArray) {
			JsonObject json = element.getAsJsonObject();
			String name = json.get("name").getAsString();
			String skyBlockApiId = json.get("skyBlockApiId").getAsString();
			reforges.put(name, skyBlockApiId);
		}
		SkyBlockItemRegistry.loadReforges(reforges);
	}

	private void applyLegacyHypixelMaterial(@NonNull JsonObject jsonObject) {
		if (jsonObject.isEmpty()) return;

		Map<String, String> minecraftIdsMapping = new HashMap<>(jsonObject.entrySet().size());
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			minecraftIdsMapping.put(entry.getKey(), entry.getValue().getAsString());
		}
		SkyBlockItemRegistry.loadLegacyHypixelMaterial(minecraftIdsMapping);
	}

	public void reload() {
		if (repository.isFetchInProgress()) {
			PlayerContext.sendMessageWithPrefix(Component.literal("A fetch is already in progress!").withStyle(ChatFormatting.YELLOW));
			return;
		}

		PlayerContext.sendMessageWithPrefix(Component.literal("Starting repository reload..").withStyle(ChatFormatting.YELLOW));
		repository.reload()
				.thenRunAsync(this::loadRepositoryData, this.executor)
				.thenRun(() -> Minecraft.getInstance().execute(this::sendReloadResult))
				.exceptionally(ex -> {
					CaribouStonks.LOGGER.error("[RepositoryDataSource] Reload failed", ex);
					return null;
				});
	}

	private void sendReloadResult() {
		if (repository.isReady()) {
			PlayerContext.sendMessageWithPrefix(Component.empty()
					.append(Component.literal("✓ Repository updated to version ").withStyle(ChatFormatting.GREEN))
					.append(Component.literal(repository.getLocalVersion()).withStyle(ChatFormatting.YELLOW)));
		} else {
			PlayerContext.sendMessageWithPrefix(Component.empty()
					.append(Component.literal("✗ Reload failed. Check logs for details or open an Issue on GitHub!").withStyle(ChatFormatting.RED)));
		}
	}
}
