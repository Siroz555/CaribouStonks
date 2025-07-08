package fr.siroz.cariboustonks.feature.stonks.search;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class StonksSearchScreen extends Screen {

	private final Screen parent;
	private ButtonWidget selectButton;
	private TextFieldWidget searchBox;
	private ItemListWidget itemListWidget;
	private final int totalSkyBlockItemsCount;

	public StonksSearchScreen(Screen parent) {
		super(Text.literal("Search for a SkyBlock item"));
		this.parent = parent;
		this.totalSkyBlockItemsCount = CaribouStonks.core().getHypixelDataSource().getSkyBlockItemCounts();
	}

	@Override
	protected void init() {
		searchBox = new TextFieldWidget(this.textRenderer,
				this.width / 2 - 100, 22,
				200, 20,
				searchBox, Text.literal("Search..."));
		searchBox.setChangedListener((search) -> itemListWidget.setSearch(search));
		addSelectableChild(searchBox);

		itemListWidget = addDrawableChild(new ItemListWidget(this, this.client,
				this.width, this.height - 112,
				48, 36,
				searchBox.getText()));

		selectButton = addDrawableChild(ButtonWidget.builder(Text.literal("Load"),
						(b) -> itemListWidget.getSelectedOptional()
								.ifPresent(ItemListWidget.ItemEntry::load))
				.dimensions(this.width / 2 - 154, this.height - 38, 150, 20).build()); // 52

		addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (b) -> {
			if (this.client != null) {
				this.client.setScreen(parent);
			}
		}).dimensions(this.width / 2 + 4, this.height - 38, 150, 20).build()); // 52

		itemSelected(null);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		searchBox.render(context, mouseX, mouseY, delta);
		Text title = Text.literal("Search for a SkyBlock item (" + this.totalSkyBlockItemsCount + " items)");
		context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, 8, Colors.WHITE.asInt());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(searchBox);
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}

	public void itemSelected(Object o) {
		if (o == null) {
			selectButton.setMessage(Text.literal("Load"));
			selectButton.active = false;
		} else {
			selectButton.setMessage(Text.literal("Load"));
			selectButton.active = true;
		}
	}
}
