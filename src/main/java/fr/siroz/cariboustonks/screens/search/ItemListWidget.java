package fr.siroz.cariboustonks.screens.search;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataException;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.screens.stonks.StonksScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class ItemListWidget extends ObjectSelectionList<ItemListWidget.Entry> {

	private final StonksSearchScreen parent;

	public static final Identifier HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("world_list/join_highlighted");
	public static final Identifier TEXTURE = Identifier.withDefaultNamespace("world_list/join");

	private final CompletableFuture<List<ItemSummary>> itemsFuture;
	private List<ItemSummary> items;
	private String search;
	static boolean loadingSearch;
	private final LoadingEntry loadingEntry;

	ItemListWidget(
            StonksSearchScreen parent,
            Minecraft client,
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
			HypixelDataSource hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();
			List<SkyBlockItemData> itemList = hypixelDataSource.getSkyBlockItems(); // HypixelDataException

			return CompletableFuture.supplyAsync(() -> {

				List<ItemSummary> itemSummaries = new ArrayList<>(itemList.size());
				for (SkyBlockItemData item : itemList) {
					String skyBlockId = item.skyBlockId();
					ChatFormatting formatting = item.tier().getFormatting();
					String name = item.name();
					ItemStack itemStack = hypixelDataSource.getItemStack(skyBlockId);

					ItemSummary summary = new ItemSummary(skyBlockId, formatting, name, itemStack);
					itemSummaries.add(summary);
				}

				return itemSummaries;
			});
		} catch (HypixelDataException exception) {
			Client.showFatalErrorScreen(Component.literal("Unable to load items!"), exception.getMessageText());
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
	public void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		List<ItemSummary> itemsList = tryGet();
		if (itemsList != items) {
			show(itemsList);
		}

		super.renderWidget(guiGraphics, mouseX, mouseY, delta);
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
	public boolean keyPressed(KeyEvent input) {
		if (input.isConfirmation()) {
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
		Entry entry = this.getSelected();
		if (entry instanceof ItemEntry itemEntry) {
			return Optional.of(itemEntry);
		} else {
			return Optional.empty();
		}
	}

	abstract static class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {
		Entry() {
		}

		@Override
		public void close() {
		}
	}

	class ItemEntry extends Entry {
		private final Minecraft client;
		private final ItemSummary item;
		private long time;

		ItemEntry(ItemListWidget itemList, ItemSummary item) {
			this.client = itemList.minecraft;
			this.item = item;
		}

		@Override
		public @NonNull Component getNarration() {
			return Component.literal("Select " + item.name());
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			String displayName = item.name();
			String name = item.hypixelSkyBlockId();
			Color color = item.color().getColor() == null ? Colors.WHITE : Color.fromFormatting(item.color());

			guiGraphics.drawString(this.client.font, displayName, this.getContentX() + 32 + 3, this.getContentY() + 1, color.asInt());
			int x1 = this.getContentX() + 32 + 3;
			guiGraphics.drawString(this.client.font, name, x1, this.getContentY() + 9 + 3, Color.fromHexString("#7f7f80").asInt());

			//context.drawItem(item.icon(), x + 7, y + 7);

			if (hovered) {
				guiGraphics.fill(this.getContentX(), this.getContentY(), this.getContentX() + 32, this.getContentY() + 32, -1601138544);
				int x2 = mouseX - this.getContentX();
				if (x2 < 32) {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ItemListWidget.HIGHLIGHTED_TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
				} else {
					guiGraphics.renderItem(item.icon(), this.getContentX() + 7, this.getContentY() + 7);
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ItemListWidget.TEXTURE, this.getContentX(), this.getContentY(), 32, 32);
				}
			} else {
				guiGraphics.renderItem(item.icon(), this.getContentX() + 7, this.getContentY() + 7);
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			setSelected(this);
			if (!(click.x() - (double) getRowLeft() <= (double) 32.0F) && Util.getMillis() - time >= 1000L) {
				time = Util.getMillis();
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
		private static final Component LOADING_LIST_TEXT = Component.literal("Loading..");
		private final Minecraft client;

		LoadingEntry(Minecraft client) {
			this.client = client;
		}

		@Override
		public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (this.client == null || this.client.screen == null) {
				return;
			}

			int x1 = (this.client.screen.width - this.client.font.width(LOADING_LIST_TEXT)) / 2;
			int y1 = this.getY() + (this.getHeight() - 9) / 2;
			guiGraphics.drawString(this.client.font, LOADING_LIST_TEXT, x1, y1,  Colors.WHITE.asInt());

			String string = LoadingDotsText.get(Util.getMillis());
			int x2 = (this.client.screen.width - this.client.font.width(string)) / 2;
			int y2 = y1 + 9;
			guiGraphics.drawString(this.client.font, string, x2, y2, Colors.GRAY.asInt());
		}

		@Override
		public @NonNull Component getNarration() {
			return LOADING_LIST_TEXT;
		}
	}

	private record ItemSummary(
            String hypixelSkyBlockId,
            ChatFormatting color,
            String name,
            ItemStack icon
	) implements Comparable<ItemSummary> {

		@Override
		public int compareTo(@NonNull ItemSummary itemSummary) {
			return this.name.compareToIgnoreCase(itemSummary.name);
		}
	}
}
