package fr.siroz.cariboustonks.feature.stonks.search;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.item.ItemLookupKey;
import fr.siroz.cariboustonks.core.data.hypixel.SkyBlockItem;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataException;
import fr.siroz.cariboustonks.feature.stonks.StonksScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ItemListWidget extends AlwaysSelectedEntryListWidget<ItemListWidget.Entry> {

	private final StonksSearchScreen parent;

	public static final Identifier HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/join_highlighted");
	public static final Identifier TEXTURE = Identifier.ofVanilla("world_list/join");

	private final CompletableFuture<List<ItemSummary>> itemsFuture;
	private List<ItemSummary> items;
	private String search;
	static boolean loadingSearch;
	private final LoadingEntry loadingEntry;

	public ItemListWidget(
			StonksSearchScreen parent,
			MinecraftClient client,
			int width,
			int height,
			int y,
			int itemHeight,
			String search
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		this.loadingEntry = new LoadingEntry(client);
		this.search = search;
		loadingSearch = false;
		this.itemsFuture = this.loadItems();
		this.show(this.tryGet());
	}

	private CompletableFuture<List<ItemSummary>> loadItems() {
		try {
			HypixelDataSource hypixelDataSource = CaribouStonks.core().getHypixelDataSource();
			List<SkyBlockItem> itemList = hypixelDataSource.getSkyBlockItems(); // HypixelDataException

			return CompletableFuture.supplyAsync(() -> {

				List<ItemSummary> itemSummaries = new ArrayList<>(itemList.size());
				for (SkyBlockItem item : itemList) {
					String skyBlockId = item.getSkyBlockId();
					Formatting formatting = item.getTier().getFormatting();
					String name = item.getName();
					ItemStack itemStack = hypixelDataSource.getItemStack(skyBlockId);

					ItemSummary summary = new ItemSummary(skyBlockId, formatting, name, itemStack);
					itemSummaries.add(summary);
				}

				return itemSummaries;
			});
		} catch (HypixelDataException exception) {
			StonksUtils.showFatalErrorScreen(Text.literal("Unable to load items!"), exception.getMessageText());
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	@Override
	protected void clearEntries() {
		this.children().forEach(ItemListWidget.Entry::close);
		super.clearEntries();
	}

	@Nullable
	private List<ItemSummary> tryGet() {
		try {
			return itemsFuture.getNow(null);
		} catch (CancellationException | CompletionException exception) {
			return null;
		}
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		List<ItemSummary> itemsList = tryGet();
		if (itemsList != items) {
			show(itemsList);
		}

		super.renderWidget(context, mouseX, mouseY, delta);
	}

	private void show(@Nullable List<ItemSummary> items) {
		if (items == null) {
			showLoadingScreen();
		} else {
			showSummaries(search, items);
		}

		this.items = items;
	}

	public void setSearch(String search) {
		if (items != null && !search.equals(this.search)) {
			showSummaries(search, items);
		}

		this.search = search;
	}

	private void showSummaries(String search, List<ItemSummary> summaries) {
		this.clearEntries();
		search = search.toLowerCase(Locale.ROOT);

		for (ItemSummary itemSummary : summaries) {
			if (shouldShow(search, itemSummary)) {
				this.addEntry(new ItemEntry(this, itemSummary));
			}
		}
	}

	private boolean shouldShow(String search, ItemSummary summary) {
		return summary.name().toLowerCase(Locale.ROOT).contains(search)
				|| summary.hypixelSkyBlockId().toLowerCase(Locale.ROOT).contains(search);
	}

	private void showLoadingScreen() {
		this.clearEntries();
		this.addEntry(loadingEntry);
	}

	@Override
	public void setSelected(@Nullable Entry entry) {
		super.setSelected(entry);

		ItemSummary item = null;
		if (entry instanceof ItemEntry itemEntry) {
			item = itemEntry.item;
		}

		parent.itemSelected(item);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (KeyCodes.isToggle(keyCode)) {
			Optional<ItemEntry> itemEntry = getSelectedOptional();
			if (itemEntry.isPresent()) {
				Client.playSoundButtonClickUI();
				itemEntry.get().load();
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public Optional<ItemEntry> getSelectedOptional() {
		Entry entry = this.getSelectedOrNull();
		if (entry instanceof ItemEntry itemEntry) {
			return Optional.of(itemEntry);
		} else {
			return Optional.empty();
		}
	}

	public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
		public Entry() {
		}

		@Override
		public void close() {
		}
	}

	public class ItemEntry extends Entry {
		private final MinecraftClient client;
		private final ItemSummary item;
		private long time;

		public ItemEntry(ItemListWidget itemList, ItemSummary item) {
			this.client = itemList.client;
			this.item = item;
		}

		@Override
		public Text getNarration() {
			return Text.literal("Select " + item.name());
		}

		@Override
		public void render(
				DrawContext context,
				int index,
				int y,
				int x,
				int entryWidth,
				int entryHeight,
				int mouseX,
				int mouseY,
				boolean hovered,
				float tickDelta
		) {
			if (this.client == null || this.client.textRenderer == null) {
				return;
			}

			String displayName = item.name();
			String name = item.hypixelSkyBlockId();
			int color = item.color().getColorValue() == null ? -1 : item.color().getColorValue();

			context.drawTextWithShadow(this.client.textRenderer, displayName, x + 32 + 3, y + 1, color);
			int x1 = x + 32 + 3;
			context.drawTextWithShadow(this.client.textRenderer, name, x1, y + 9 + 3, -8355712);

			//context.drawItem(item.icon(), x + 7, y + 7);

			if (hovered) {
				context.fill(x, y, x + 32, y + 32, -1601138544);
				int x2 = mouseX - x;
				if (x2 < 32) {
					context.drawGuiTexture(RenderLayer::getGuiTextured, ItemListWidget.HIGHLIGHTED_TEXTURE, x, y, 32, 32);
				} else {
					context.drawItem(item.icon(), x + 7, y + 7);
					context.drawGuiTexture(RenderLayer::getGuiTextured, ItemListWidget.TEXTURE, x, y, 32, 32);
				}
			} else {
				context.drawItem(item.icon(), x + 7, y + 7);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			setSelected(this);
			if (!(mouseX - (double) getRowLeft() <= (double) 32.0F) && Util.getMeasuringTimeMs() - time >= 1000L) {
				time = Util.getMeasuringTimeMs();
				return super.mouseClicked(mouseX, mouseY, button);
			} else {
				Client.playSoundButtonClickUI();
				load();
				return true;
			}
		}

		public void load() {
			if (loadingSearch) {
				return;
			}

			loadingSearch = true;

			if (client.player != null) {
				client.setScreen(new StonksScreen(ItemLookupKey.of(
						NotEnoughUpdatesUtils.getNeuIdFromSkyBlockId(item.hypixelSkyBlockId()),
						item.hypixelSkyBlockId()
				)));
			}
		}
	}

	public static class LoadingEntry extends Entry {
		private static final Text LOADING_LIST_TEXT = Text.literal("Loading..");
		private final MinecraftClient client;

		public LoadingEntry(MinecraftClient client) {
			this.client = client;
		}

		@Override
		public void render(
				DrawContext context,
				int index,
				int y,
				int x,
				int entryWidth,
				int entryHeight,
				int mouseX,
				int mouseY,
				boolean hovered,
				float tickDelta
		) {
			if (this.client == null || this.client.currentScreen == null || this.client.textRenderer == null) {
				return;
			}

			int x1 = (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2;
			int y1 = y + (entryHeight - 9) / 2;
			context.drawTextWithShadow(this.client.textRenderer, LOADING_LIST_TEXT, x1, y1, -1);

			String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
			int x2 = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
			int y2 = y1 + 9;
			context.drawTextWithShadow(this.client.textRenderer, string, x2, y2, -8355712);
		}

		public Text getNarration() {
			return LOADING_LIST_TEXT;
		}
	}
}
