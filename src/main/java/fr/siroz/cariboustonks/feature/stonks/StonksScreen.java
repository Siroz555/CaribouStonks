package fr.siroz.cariboustonks.feature.stonks;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.data.generic.ItemPrice;
import fr.siroz.cariboustonks.core.data.item.ItemLookupKey;
import fr.siroz.cariboustonks.core.data.hypixel.SkyBlockItem;
import fr.siroz.cariboustonks.feature.stonks.graph.ItemGraphWidget;
import fr.siroz.cariboustonks.feature.stonks.info.ItemInformationWidget;
import fr.siroz.cariboustonks.feature.stonks.search.StonksSearchScreen;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StonksScreen extends CaribousStonksScreen {

	private static final Identifier MENU_LIST_BACKGROUND_TEXTURE
			= Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");

	private final ItemStack icon;
	private final Text itemName;

	private ItemInformationWidget itemInformationWidget;
	private ItemGraphWidget itemGraphWidget;
	private List<ItemPrice> graphData = null;
	private Product bazaarItem = null;

	private volatile boolean notFound = false;

	public StonksScreen(@NotNull ItemLookupKey key) {
		super(Text.literal("Stonks"));

		HypixelDataSource hypixelDataSource = CaribouStonks.core().getHypixelDataSource();
		Optional<SkyBlockItem> item = hypixelDataSource.getSkyBlockItemOptional(key.getHypixelSkyBlockId());
		if (item.isPresent()) {
			this.itemName = Text.literal(item.get().getName()).formatted(item.get().getTier().getFormatting());
			this.icon = hypixelDataSource.getItemStack(item.get().getSkyBlockId());
		} else {
			this.itemName = Text.literal(key.getHypixelSkyBlockId() != null ? key.getHypixelSkyBlockId() : "? ? ?");
			this.icon = new ItemStack(Items.BARRIER);
		}

		this.fetchItemData(key).thenRun(this::initStonksWidgets);
	}

	private void initStonksWidgets() {
		if (notFound) {
			return;
		}

		CompletableFuture<Void> graphFuture = CompletableFuture.runAsync(() ->
				itemGraphWidget = new ItemGraphWidget(
						graphData, getGraphWidgetWidth(), getGraphWidgetHeight())
		);
		CompletableFuture<Void> informationsFuture = CompletableFuture.runAsync(() ->
				itemInformationWidget = new ItemInformationWidget(
						bazaarItem, getInformationWidgetWidth(), getInformationWidgetHeight())
		);

		CompletableFuture.allOf(graphFuture, informationsFuture).thenRun(() -> {
			synchronized (this) {
				clearAndInit();
			}
		});
	}

	@Override
	protected void onInit() {
		ThreePartsLayoutWidget header = new ThreePartsLayoutWidget(this, 30, 0);

		Text headerText = graphData != null ? itemName : Text.literal("...").formatted(Formatting.WHITE);

		header.addHeader(new IconTextWidget(headerText, textRenderer, icon));

		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(3);

		Text granularity = itemGraphWidget != null
				? Text.literal(itemGraphWidget.getGranularity()).formatted(Formatting.GREEN)
				: Text.literal("?").formatted(Formatting.BOLD);

		ButtonWidget graphType = ButtonWidget.builder(Text.literal("Graph type : ").append(granularity), b -> {
			if (itemGraphWidget != null && itemGraphWidget.updateGranularity()) {
				refreshWidgetPositions();
			}
		}).tooltip(Tooltip.of(Text.literal("Change Graph Display")
				.append("\n\n")
				.append("Hour: Price of 1 hour").append("\n")
				.append("Day: Price of 1 day").append("\n")
				.append("Week: Price of 1 week").append("\n")
				.append("Month: Price of 1 month")
		)).build();
		adder.add(graphType);

		ButtonWidget search = ButtonWidget.builder(Text.literal("Search an item.."), _b ->
				Objects.requireNonNull(client).setScreen(new StonksSearchScreen(null))
		).build();
		adder.add(search);

		ButtonWidget buttonDone = ButtonWidget.builder(ScreenTexts.DONE, _b -> close()).build();
		adder.add(buttonDone);

		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.forEachChild(this::addDrawableChild);

		header.refreshPositions();
		header.forEachChild(this::addDrawableChild);
	}

	@Override
	public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
		synchronized (this) {
			super.onRender(context, mouseX, mouseY, delta);
		}

		if (graphData == null) {
			if (notFound) {
				Text error = Text.literal("Error ;(");
				int x1 = (getBackgroundWidth() - textRenderer.getWidth(error)) / 2;
				int y1 = getBackgroundHeight() / 2;
				context.drawTextWithShadow(textRenderer, error, x1, y1, Color.RED.getRGB());
			} else {
				showLoadingScreen(context);
			}
		}

		// ListWidget background texture
		context.drawTexture(
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
		context.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				Screen.INWORLD_HEADER_SEPARATOR_TEXTURE,
				getBackgroundX(),
				getBackgroundY() - 2,
				0.0F,
				0.0F,
				getBackgroundWidth(),
				2,
				32,
				2);

		// ListWidget separators textures - footer
		context.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE,
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
			itemGraphWidget.render(context, mouseX, mouseY, getBackgroundX(), getBackgroundY());
		}

		if (itemInformationWidget != null) {
			itemInformationWidget.updateWidgetPosition(getInformationWidgetWidth(), getInformationWidgetHeight());
			itemInformationWidget.render(context, mouseX, mouseY, getBackgroundWidth() / 2 + 50, getBackgroundY());
		}
	}

	@Override
	public void onClose() {
		if (client != null) {
			client.setScreen(null);
		}
	}

	private void showLoadingScreen(DrawContext context) {
		if (client == null || client.currentScreen == null) {
			return;
		}

		Text loadingText = Text.literal("Loading..");

		int x1 = (client.currentScreen.width - textRenderer.getWidth(loadingText)) / 2;
		int y1 = getBackgroundHeight() / 2;
		context.drawTextWithShadow(textRenderer, loadingText, x1, y1, Colors.WHITE.asInt());

		String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
		int x2 = (client.currentScreen.width - textRenderer.getWidth(string)) / 2;
		int y2 = y1 + 9;
		context.drawTextWithShadow(textRenderer, string, x2, y2, Colors.WHITE.asInt());
	}

	private @NotNull CompletableFuture<Void> fetchItemData(@NotNull ItemLookupKey key) {
		CompletableFuture<Void> priceHistory = CaribouStonks.core().getGenericDataSource()
				.loadGraphData(key)
				.thenAccept(data -> {
					if (data == null || data.isEmpty()) notFound = true;
					graphData = data;
				});

		bazaarItem = CaribouStonks.core().getHypixelDataSource()
				.getBazaarItem(key.getHypixelSkyBlockId())
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

	private static class IconTextWidget extends TextWidget {

		private final ItemStack icon;

		IconTextWidget(Text message, TextRenderer textRenderer, ItemStack icon) {
			super(message, textRenderer);
			this.icon = icon;
		}

		@Override
		public void renderWidget(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
			Text text = this.getMessage();
			TextRenderer textRenderer = this.getTextRenderer();

			int width = this.getWidth();
			int textWidth = textRenderer.getWidth(text);
			float horizontalAlignment = 0.5f;
			int x = this.getX() + 17 + Math.round(horizontalAlignment * (float) (width - textWidth));
			int y = this.getY() + (this.getHeight() - textRenderer.fontHeight) / 2;
			OrderedText orderedText = textWidth > width ? this.trim(text, width) : text.asOrderedText();

			context.drawTextWithShadow(textRenderer, orderedText, x, y, this.getTextColor());
			context.drawItem(icon, x - 34, y - 4);
		}

		private OrderedText trim(Text text, int width) {
			TextRenderer textRenderer = this.getTextRenderer();
			StringVisitable stringVisitable = textRenderer.trimToWidth(
					text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS));
			return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS));
		}
	}
}
