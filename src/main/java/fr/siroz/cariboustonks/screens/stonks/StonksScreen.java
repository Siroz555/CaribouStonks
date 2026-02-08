package fr.siroz.cariboustonks.screens.stonks;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.data.generic.ItemPrice;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.screens.CaribousStonksScreen;
import fr.siroz.cariboustonks.screens.search.StonksSearchScreen;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class StonksScreen extends CaribousStonksScreen {

	private static final Identifier MENU_LIST_BACKGROUND_TEXTURE
			= Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

	private final ItemStack icon;
	private final Component itemName;

	private InformationWidget itemInformationWidget;
	private GraphWidget itemGraphWidget;
	private List<ItemPrice> graphData = null;
	private BazaarProduct bazaarItem = null;

	private volatile boolean notFound = false;

	private StonksScreen(@NonNull ItemLookupKey key) {
		super(Component.literal("Stonks"));

		HypixelDataSource hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();
		Optional<SkyBlockItemData> item = hypixelDataSource.getSkyBlockItemOptional(key.hypixelSkyBlockId());
		if (item.isPresent()) {
			this.itemName = Component.literal(item.get().name()).withStyle(item.get().tier().getFormatting());
			this.icon = hypixelDataSource.getItemStack(item.get().skyBlockId());
		} else {
			this.itemName = Component.literal(key.hypixelSkyBlockId() != null ? key.hypixelSkyBlockId() : "? ? ?");
			this.icon = new ItemStack(Items.BARRIER);
		}

		this.fetchItemData(key).thenRun(this::initStonksWidgets);
	}

	public static @NonNull StonksScreen create(@NonNull ItemLookupKey key) {
		return new StonksScreen(key);
	}

	private void initStonksWidgets() {
		if (notFound) {
			return;
		}

		CompletableFuture<Void> graphFuture = CompletableFuture.runAsync(() ->
				itemGraphWidget = new GraphWidget(
						graphData, getGraphWidgetWidth(), getGraphWidgetHeight())
		);
		CompletableFuture<Void> informationsFuture = CompletableFuture.runAsync(() ->
				itemInformationWidget = new InformationWidget(
						bazaarItem, getInformationWidgetWidth(), getInformationWidgetHeight())
		);

		CompletableFuture.allOf(graphFuture, informationsFuture).thenRun(() -> {
			synchronized (this) {
				rebuildWidgets();
			}
		});
	}

	@Override
	protected void onInit() {
		HeaderAndFooterLayout header = new HeaderAndFooterLayout(this, 30, 0);

		Component headerText = graphData != null ? itemName : Component.literal("...").withStyle(ChatFormatting.WHITE);

		header.addToHeader(new IconTextWidget(headerText, font, icon));

		GridLayout gridWidget = new GridLayout();
		gridWidget.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(3);

		Component granularity = itemGraphWidget != null
				? Component.literal(itemGraphWidget.getGranularity()).withStyle(ChatFormatting.GREEN)
				: Component.literal("?").withStyle(ChatFormatting.BOLD);

		Button graphType = Button.builder(Component.literal("Graph type : ").append(granularity), b -> {
			if (itemGraphWidget != null && itemGraphWidget.updateGranularity()) {
				repositionElements();
			}
		}).tooltip(Tooltip.create(Component.literal("Change Graph Display")
				.append("\n\n")
				.append("Hour: Price of 1 hour").append("\n")
				.append("Day: Price of 1 day").append("\n")
				.append("Week: Price of 1 week").append("\n")
				.append("Month: Price of 1 month")
		)).build();
		adder.addChild(graphType);

		Button search = Button.builder(Component.literal("Search an item.."), _b ->
				Objects.requireNonNull(minecraft).setScreen(new StonksSearchScreen(null))
		).build();
		adder.addChild(search);

		Button buttonDone = Button.builder(CommonComponents.GUI_DONE, _b -> close()).build();
		adder.addChild(buttonDone);

		gridWidget.arrangeElements();
		FrameLayout.centerInRectangle(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.visitWidgets(this::addRenderableWidget);

		header.arrangeElements();
		header.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		synchronized (this) {
			super.onRender(guiGraphics, mouseX, mouseY, delta);
		}

		if (graphData == null) {
			if (notFound) {
				Component error = Component.literal("Error ;(");
				int x1 = (getBackgroundWidth() - font.width(error)) / 2;
				int y1 = getBackgroundHeight() / 2;
				guiGraphics.drawString(font, error, x1, y1, Color.RED.getRGB());
			} else {
				showLoadingScreen(guiGraphics);
			}
		}

		// ListWidget background texture
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				MENU_LIST_BACKGROUND_TEXTURE,
				getBackgroundX(), getBackgroundY(),
				(float) getBackgroundRight(),
				(float) getBackgroundBottom(),
				width,
				getBackgroundHeight(),
				32,
				32);

		// ListWidget separators textures - header
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				Screen.INWORLD_HEADER_SEPARATOR,
				getBackgroundX(),
				getBackgroundY() - 2,
				0.0F,
				0.0F,
				getBackgroundWidth(),
				2,
				32,
				2);

		// ListWidget separators textures - footer
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				Screen.INWORLD_FOOTER_SEPARATOR,
				getBackgroundX(),
				getBackgroundBottom(),
				0.0F,
				0.0F,
				getBackgroundWidth(),
				2,
				32,
				2);

		if (itemGraphWidget != null) {
			itemGraphWidget.updateWidgetPosition(getGraphWidgetWidth(), getGraphWidgetHeight());
			itemGraphWidget.render(guiGraphics, mouseX, mouseY, getBackgroundX(), getBackgroundY());
		}

		if (itemInformationWidget != null) {
			itemInformationWidget.updateWidgetPosition(getInformationWidgetWidth(), getInformationWidgetHeight());
			itemInformationWidget.render(guiGraphics, mouseX, mouseY, getBackgroundWidth() / 2 + 50, getBackgroundY());
		}
	}

	@Override
	public void close() {
		minecraft.setScreen(null);
	}

	private void showLoadingScreen(GuiGraphics context) {
		if (minecraft.screen == null) {
			return;
		}

		Component loadingText = Component.literal("Loading..");

		int x1 = (minecraft.screen.width - font.width(loadingText)) / 2;
		int y1 = getBackgroundHeight() / 2;
		context.drawString(font, loadingText, x1, y1, Colors.WHITE.asInt());

		String string = LoadingDotsText.get(Util.getMillis());
		int x2 = (minecraft.screen.width - font.width(string)) / 2;
		int y2 = y1 + 9;
		context.drawString(font, string, x2, y2, Colors.WHITE.asInt());
	}

	private @NonNull CompletableFuture<Void> fetchItemData(@NonNull ItemLookupKey key) {
		CompletableFuture<Void> priceHistory = CaribouStonks.skyBlock().getGenericDataSource()
				.loadGraphData(key)
				.thenAccept(data -> {
					if (data == null || data.isEmpty()) notFound = true;
					graphData = data;
				});

		bazaarItem = CaribouStonks.skyBlock().getHypixelDataSource()
				.getBazaarItem(key.hypixelSkyBlockId())
				.orElse(null);

		/*CompletableFuture<Void> auctionItem;
		if (bazaarItem != null) {
			auctionItem = CompletableFuture.completedFuture(null);
		} else {
			auctionItem = CaribouStonks.core()
					.getCoreComponent(GenericDataSource.class)
					.loadGraphData(null, null)
					.thenAccept(instantPriceObjectMap -> {

					});
		}*/

		return CompletableFuture.allOf(priceHistory);
	}

	private int getBackgroundX() {
		return 0;
	}

	private int getBackgroundY() {
		return 32;
	}

	private int getBackgroundWidth() {
		return this.width;
	}

	private int getBackgroundHeight() {
		return this.height - 96;
	}

	private int getBackgroundRight() {
		return this.width;
	}

	private int getBackgroundBottom() {
		return 32 + (this.height - 96);
	}

	private int getGraphWidgetWidth() {
		return getBackgroundWidth() - 30; // -50
	}

	private int getGraphWidgetHeight() {
		return (int) (getBackgroundHeight() * 1.5);
	}

	private int getInformationWidgetWidth() {
		return getBackgroundWidth() - getBackgroundWidth() / 2 + 50;
	}

	private int getInformationWidgetHeight() {
		return (int) (getBackgroundHeight() * 1.5);
	}

	private static class IconTextWidget extends StringWidget {

		private final ItemStack icon;

		IconTextWidget(Component message, Font textRenderer, ItemStack icon) {
			super(message, textRenderer);
			this.icon = icon;
		}

		@Override
		public void renderWidget(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
			Component text = this.getMessage();
			Font textRenderer = this.getFont();

			int width = this.getWidth();
			int textWidth = textRenderer.width(text);
			float horizontalAlignment = 0.5f;
			int x = this.getX() + 17 + Math.round(horizontalAlignment * (float) (width - textWidth));
			int y = this.getY() + (this.getHeight() - textRenderer.lineHeight) / 2;
			FormattedCharSequence orderedText = textWidth > width ? this.trim(text, width) : text.getVisualOrderText();

			context.drawString(textRenderer, orderedText, x, y, Colors.WHITE.asInt());
			context.renderItem(icon, x - 34, y - 4);
		}

		private FormattedCharSequence trim(Component text, int width) {
			Font textRenderer = this.getFont();
			FormattedText stringVisitable = textRenderer.substrByWidth(
					text, width - textRenderer.width(CommonComponents.ELLIPSIS));
			return Language.getInstance().getVisualOrder(FormattedText.composite(stringVisitable, CommonComponents.ELLIPSIS));
		}
	}
}
