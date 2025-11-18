package fr.siroz.cariboustonks.screen.search;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.screen.stonks.StonksScreen;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.core.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataException;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

class ItemListWidget extends AlwaysSelectedEntryListWidget<ItemListWidget.Entry> {

	private final StonksSearchScreen parent;

	public static final Identifier HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/join_highlighted");
	public static final Identifier TEXTURE = Identifier.ofVanilla("world_list/join");

	private final CompletableFuture<List<ItemSummary>> itemsFuture;
	private List<ItemSummary> items;
	private String search;
	static boolean loadingSearch;
	private final LoadingEntry loadingEntry;

	ItemListWidget(
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
			List<SkyBlockItemData> itemList = hypixelDataSource.getSkyBlockItems(); // HypixelDataException

			return CompletableFuture.supplyAsync(() -> {

				List<ItemSummary> itemSummaries = new ArrayList<>(itemList.size());
				for (SkyBlockItemData item : itemList) {
					String skyBlockId = item.skyBlockId();
					Formatting formatting = item.tier().getFormatting();
					String name = item.name();
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
	public boolean keyPressed(KeyInput input) {
		if (input.isEnter()) {
			Optional<ItemEntry> itemEntry = getSelectedOptional();
			if (itemEntry.isPresent()) {
				Client.playSoundButtonClickUI();
				itemEntry.get().load();
				return true;
			}
		}

		return super.keyPressed(input);
	}

	public Optional<ItemEntry> getSelectedOptional() {
		Entry entry = this.getSelectedOrNull();
		if (entry instanceof ItemEntry itemEntry) {
			return Optional.of(itemEntry);
		} else {
			return Optional.empty();
		}
	}

	abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
		public Entry() {
		}

		@Override
		public void close() {
		}
	}

	class ItemEntry extends Entry {
		private final MinecraftClient client;
		private final ItemSummary item;
		private long time;

		ItemEntry(ItemListWidget itemList, ItemSummary item) {
			this.client = itemList.client;
			this.item = item;
		}

		@Override
		public Text getNarration() {
			return Text.literal("Select " + item.name());
		}

		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (this.client == null || this.client.textRenderer == null) {
				return;
			}

			String displayName = item.name();
			String name = item.hypixelSkyBlockId();
			Color color = item.color().getColorValue() == null ? Colors.WHITE : Color.fromFormatting(item.color());

			context.drawTextWithShadow(this.client.textRenderer, displayName, this.getContentX() + 32 + 3, this.getContentY() + 1, color.asInt());
			int x1 = this.getContentX() + 32 + 3;
			context.drawTextWithShadow(this.client.textRenderer, name, x1, this.getContentY() + 9 + 3, Color.fromHexString("#7f7f80").asInt());

			//context.drawItem(item.icon(), x + 7, y + 7);

			if (hovered) {
				context.fill(this.getContentX(), this.getContentY(), this.getContentX() + 32, this.getContentY() + 32, -1601138544);
				int x2 = mouseX - this.getContentX();
				if (x2 < 32) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ItemListWidget.HIGHLIGHTED_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
				} else {
					context.drawItem(item.icon(), this.getContentX() + 7, this.getContentY() + 7);
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ItemListWidget.TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
				}
			} else {
				context.drawItem(item.icon(), this.getContentX() + 7, this.getContentY() + 7);
			}
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			setSelected(this);
			if (!(click.x() - (double) getRowLeft() <= (double) 32.0F) && Util.getMeasuringTimeMs() - time >= 1000L) {
				time = Util.getMeasuringTimeMs();
				return super.mouseClicked(click, doubled);
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
				client.setScreen(StonksScreen.create(ItemLookupKey.of(
						NotEnoughUpdatesUtils.getNeuIdFromSkyBlockId(item.hypixelSkyBlockId()),
						item.hypixelSkyBlockId()
				)));
			}
		}
	}

	static class LoadingEntry extends Entry {
		private static final Text LOADING_LIST_TEXT = Text.literal("Loading..");
		private final MinecraftClient client;

		public LoadingEntry(MinecraftClient client) {
			this.client = client;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (this.client == null || this.client.currentScreen == null || this.client.textRenderer == null) {
				return;
			}

			int x1 = (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2;
			int y1 = this.getY() + (this.getHeight() - 9) / 2;
			context.drawTextWithShadow(this.client.textRenderer, LOADING_LIST_TEXT, x1, y1,  Colors.WHITE.asInt());

			String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
			int x2 = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
			int y2 = y1 + 9;
			context.drawTextWithShadow(this.client.textRenderer, string, x2, y2, Colors.GRAY.asInt());
		}

		public Text getNarration() {
			return LOADING_LIST_TEXT;
		}
	}

	private record ItemSummary(
			String hypixelSkyBlockId,
			Formatting color,
			String name,
			ItemStack icon
	) implements Comparable<ItemSummary> {

		@Override
		public int compareTo(@NotNull ItemSummary itemSummary) {
			return this.name.compareToIgnoreCase(itemSummary.name);
		}
	}
}
