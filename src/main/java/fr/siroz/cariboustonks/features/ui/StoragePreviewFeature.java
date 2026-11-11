package fr.siroz.cariboustonks.features.ui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.GuiEvents;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemUtil;
import fr.siroz.cariboustonks.util.MinecraftUtil;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

public class StoragePreviewFeature extends Feature {
	private static final Pattern ENDERCHEST_PATTERN = Pattern.compile("Ender Chest.*\\((\\d+)/\\d+\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern BACKPACK_PATTERN = Pattern.compile("Backpack.*\\(Slot #(\\d+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern STRORAGE_PATTERN = Pattern.compile("Storage", Pattern.CASE_INSENSITIVE);
	private static final Pattern BACKPACK_SIZE_PATTERN = Pattern.compile("has (\\d+) slots", Pattern.CASE_INSENSITIVE);
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final Duration SAVE_RETRY_DELAY = Duration.ofSeconds(30);
	private static final int STORAGE_SIZE = 27;

	private final @Nullable Storage[] storages = new Storage[STORAGE_SIZE];
	private final ExecutorService ioExecutor;

	private @Nullable Path savePath;
	private @Nullable String loadedProfileId;

	public StoragePreviewFeature() {
		// Un seul, pour l'ordre et les écritures non concurrentes, pas de Virtual c useless
		this.ioExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r, "CaribouStonks-storage-io");
			thread.setDaemon(true);
			thread.setUncaughtExceptionHandler((t, ex) ->
					CaribouStonks.LOGGER.error("[StoragePreview] Uncaught exception in {}", t.getName(), ex));
			return thread;
		});
		GuiEvents.MOUSE_OVER_TOOLTIP_EVENT.register(this::onMouseOverTooltip);
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			if (screen instanceof AbstractContainerScreen<?> menu) {
				ScreenEvents.remove(screen).register(_ -> this.onScreenUpdate(menu));
			}
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> this.ioExecutor.shutdown());
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock() && this.config().uiAndVisuals.storagePreview;
	}

	@Override
	protected void onSecondPassed() {
		if (!isEnabled() || ioExecutor.isShutdown()) return;

		// Flush l'ancien profil avant de swap
		saveStorages();

		String skyBlockProfileId = this.skyBlock().getSkyBlockProfileId();
		if (skyBlockProfileId.isBlank()) return; // profil pas encore connu, pas touche à la racine

		String profileId = ClientContext.getProfileId().toString().replace("-", "") + "/" + skyBlockProfileId;
		if (profileId.equals(loadedProfileId)) return;

		loadedProfileId = profileId;
		savePath = CaribouStonks.CONFIG_DIR.resolve("storage-preview").resolve(profileId);
		loadStorages();
	}

	@EventHandler(event = "GuiEvents.MOUSE_OVER_TOOLTIP_EVENT")
	private boolean onMouseOverTooltip(GuiGraphicsExtractor graphics, Screen screen, int index, int mouseX, int mouseY) {
		if (!screen.getTitle().getString().equals("Storage")) return true;
		if (!isEnabled()) return true;

		if (index >= 9 && index < 18) index -= 9;
		else if (index >= 27 && index < 45) index -= 18;
		else return true;

		Storage storage = storages[index];
		if (storage == null) return true;

		int rows = (storage.size() - 9) / 9;
		int x = mouseX + 184 >= screen.width ? mouseX - 188 : mouseX + 8;
		int y = Math.max(0, mouseY - 16);

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 176, rows * 18 + 17, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + rows * 18 + 17, 0, 215, 176, 7, 256, 256);
		graphics.text(MINECRAFT.font, storage.name(), x + 8, y + 6, Colors.WHITE_RGB, false);

		for (int i = 9; i < storage.size(); ++i) {
			ItemStack currentStack = storage.getStack(i);
			int itemX = x + (i - 9) % 9 * 18 + 8;
			int itemY = y + (i - 9) / 9 * 18 + 18;
			graphics.item(currentStack, itemX, itemY);
			graphics.itemDecorations(MINECRAFT.font, currentStack, itemX, itemY);
		}

		return false;
	}

	@EventHandler(event = "ScreenEvents.AFTER_INIT")
	private void onScreenUpdate(AbstractContainerScreen<?> containerScreen) {
		if (!isEnabled()) return;

		String title = containerScreen.getTitle().getString();
		int index = getStorageIndexFromTitle(title);
		if (index >= 0 && index < STORAGE_SIZE) { // au cas où on a + de storage, mais je n'y crois pas
			storages[index] = new Storage(containerScreen.getMenu().slots.getFirst().container, title, true);
		}

		if (STRORAGE_PATTERN.matcher(title).matches()) {
			initializeStorage(containerScreen);
		}
	}

	private static int getStorageIndexFromTitle(String title) {
		Matcher enderChestMatcher = ENDERCHEST_PATTERN.matcher(title);
		if (enderChestMatcher.find()) {
			try {
				return Integer.parseInt(enderChestMatcher.group(1)) - 1;
			} catch (Exception _) {
			}
		}
		Matcher backpackMatcher = BACKPACK_PATTERN.matcher(title);
		if (backpackMatcher.find()) {
			try {
				return Integer.parseInt(backpackMatcher.group(1)) + 8;
			} catch (Exception _) {
			}
		}
		return -1;
	}

	private void initializeStorage(AbstractContainerScreen<?> containerScreen) {
		NonNullList<Slot> slots = containerScreen.getMenu().slots;
		// Ender Chests
		for (int i = 9; i < 18; ++i) {
			Slot slot = slots.get(i);
			int index = i - 9;
			// ignore les Ender Chest non débloqué et ceux déjà créés
			if (slot.getItem().is(Items.STAINED_GLASS_PANE.red()) || storages[index] != null) {
				continue;
			}
			storages[index] = new Storage(
					new SimpleContainer(Stream.generate(() -> ItemStack.EMPTY)
							.limit(18)
							.toArray(ItemStack[]::new)),
					"", true
			);
		}
		// Backpacks
		for (int i = 27; i < 45; ++i) {
			Slot slot = slots.get(i);
			int index = i - 18;
			// Pareil que Ender Chest, mais s'il n'y a plus de slot, puis les new
			if (slot.getItem().is(Items.STAINED_GLASS_PANE.brown())) {
				storages[index] = null;
			}
			if (storages[index] != null) {
				continue;
			}
			Matcher sizeMatcher = ItemUtil.getLoreLineIfMatch(slot.getItem(), BACKPACK_SIZE_PATTERN);
			if (sizeMatcher != null) {
				storages[index] = new Storage(
						new SimpleContainer(Stream.generate(() -> ItemStack.EMPTY)
								.limit(NumberUtils.toInt(sizeMatcher.group(1)) + 9)
								.toArray(ItemStack[]::new)),
						"", true
				);
			}
		}
	}

	private void loadStorages() {
		Arrays.fill(storages, null);
		Path directory = savePath;
		String profileId = loadedProfileId;
		if (directory == null || profileId == null) return;

		for (int index = 0; index < STORAGE_SIZE; ++index) {
			loadStorage(index, directory, profileId);
		}
	}

	private void loadStorage(int index, Path directory, String profileId) {
		ioExecutor.execute(() -> {
			Path file = directory.resolve(index + ".nbt");
			if (!Files.isRegularFile(file)) return;

			CompoundTag tag;
			try {
				tag = NbtIo.read(file);
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[{}] Failed to read storage preview file: {}", getShortName(), file, ex);
				return;
			}
			if (tag == null) return;

			MINECRAFT.execute(() -> onStorageLoaded(index, profileId, file, tag));
		});
	}

	private void onStorageLoaded(int index, String profileId, Path file, CompoundTag tag) {
		// Profil changé entre temps, ou storage déjà ouvert en jeu, donc plus récent
		if (!profileId.equals(loadedProfileId) || storages[index] != null) return;
		try {
			Storage.CODEC.parse(getOps(), tag)
					.ifSuccess(storage -> storages[index] = storage)
					.ifError(error -> CaribouStonks.LOGGER.error(
							"[{}] Invalid storage preview file {}: {}", getShortName(), file, error.message()));
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[{}] Failed to decode storage preview file: {}", getShortName(), file, ex);
		}
	}

	private void saveStorages() {
		Instant now = Instant.now();
		for (int index = 0; index < STORAGE_SIZE; ++index) {
			Storage storage = storages[index]; // copie locale, nik l'IDE
			if (storage != null && storage.canSave(now)) {
				saveStorage(index, storage);
			}
		}
	}

	private void saveStorage(int index, Storage storage) {
		Path directory = savePath; // lambda be like
		if (directory == null) return;

		CompoundTag tag = encode(storage);
		if (tag == null) {
			storage.onSaveFinished(false);
			return;
		}

		Path file = directory.resolve(index + ".nbt");
		storage.saving = true;
		ioExecutor.execute(() -> {
			boolean success = false;
			try {
				writeAtomically(file, tag);
				success = true;
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[{}] Failed to save storage preview file: {}", getShortName(), file, ex);
			}
			boolean saved = success;
			MINECRAFT.execute(() -> storage.onSaveFinished(saved));
		});
	}

	private @Nullable CompoundTag encode(Storage storage) {
		try {
			Tag tag = Storage.CODEC.encodeStart(getOps(), storage).getOrThrow();
			return tag instanceof CompoundTag compound ? compound : null; // j'ai trop peur avec les codecs MC
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[{}] Failed to encode storage preview", getShortName(), ex);
			return null;
		}
	}

	private static void writeAtomically(Path file, CompoundTag tag) throws IOException {
		Files.createDirectories(file.getParent());
		Path temp = file.resolveSibling(file.getFileName() + ".tmp");
		NbtIo.write(tag, temp);
		try {
			Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException _) {
			Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static RegistryOps<Tag> getOps() {
		return DeveloperTools.getRegistryLookup().createSerializationContext(NbtOps.INSTANCE);
	}

	private static class Storage {
		private static final Codec<Storage> CODEC = RecordCodecBuilder.<Storage>create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(Storage::name),
				MinecraftUtil.EMPTY_ITEMSTACK_CODEC.listOf().fieldOf("items").forGetter(Storage::getItemList)
		).apply(instance, Storage::create)).validate(Storage::validate);
		private final Container inventory;
		private final String name;
		// à partir de là c le Thread Client, il n'y a que MINECRAFT.execute() qui touche
		private boolean dirty;
		private boolean saving;
		private Instant retryAfter = Instant.MIN;

		private Storage(Container inventory, String name, boolean dirty) {
			this.inventory = inventory;
			this.name = name;
			this.dirty = dirty;
		}

		private String name() {
			return name;
		}

		public int size() {
			return inventory.getContainerSize();
		}

		public ItemStack getStack(int index) {
			return inventory.getItem(index);
		}

		private List<ItemStack> getItemList() {
			List<ItemStack> items = new ArrayList<>();
			for (int i = 0; i < size(); ++i) items.add(getStack(i));
			return items;
		}

		private boolean canSave(Instant now) {
			return dirty && !saving && now.isAfter(retryAfter);
		}

		private void onSaveFinished(boolean success) {
			saving = false;
			if (success) {
				dirty = false;
			} else {
				retryAfter = Instant.now().plus(SAVE_RETRY_DELAY); // reste dirty, retry espacé (évite le spam..)
			}
		}

		private static Storage create(String name, List<ItemStack> items) {
			SimpleContainer inventory = new SimpleContainer(items.toArray(ItemStack[]::new));
			return new Storage(inventory, name, false);
		}

		private static DataResult<Storage> validate(Storage storage) {
			int size = storage.size();
			// Pour éviter de casser le rendu si jamais il est corrompu ou édité à la main
			return size >= 18 && size % 9 == 0
					? DataResult.success(storage)
					: DataResult.error(() -> "Invalid storage size: " + size);
		}
	}
}
