package fr.siroz.cariboustonks.screen.search;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class StonksSearchScreen extends CaribousStonksScreen {

	private final Screen parent;
	private Button selectButton;
	private EditBox searchBox;
	private ItemListWidget itemListWidget;
	private final int totalSkyBlockItemsCount;

	public StonksSearchScreen(Screen parent) {
		super(Component.literal("Search for a SkyBlock item"));
		this.parent = parent;
		this.totalSkyBlockItemsCount = CaribouStonks.core().getHypixelDataSource().getSkyBlockItemCounts();
	}

	@Override
	protected void onInit() {
		searchBox = new EditBox(this.font,
				this.width / 2 - 100, 22,
				200, 20,
				searchBox, Component.literal("Search..."));
		searchBox.setResponder((search) -> itemListWidget.setSearch(search));
		addWidget(searchBox);

		itemListWidget = addRenderableWidget(new ItemListWidget(this, this.minecraft,
				this.width, this.height - 112,
				48, 36,
				searchBox.getValue()));

		selectButton = addRenderableWidget(Button.builder(Component.literal("Load"),
						(b) -> itemListWidget.getSelectedOptional()
								.ifPresent(ItemListWidget.ItemEntry::load))
				.bounds(this.width / 2 - 154, this.height - 38, 150, 20).build()); // 52

		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (b) -> this.minecraft.setScreen(parent))
				.bounds(this.width / 2 + 4, this.height - 38, 150, 20)
				.build()); // 52

		itemSelected(null);
	}

	@Override
	public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
		super.onRender(guiGraphics, mouseX, mouseY, deltaTicks);
		searchBox.render(guiGraphics, mouseX, mouseY, deltaTicks);
		Component title = Component.literal("Search for a SkyBlock item (" + this.totalSkyBlockItemsCount + " items)");
		guiGraphics.drawCenteredString(this.font, title, this.width / 2, 8, Colors.WHITE.asInt());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(searchBox);
	}

	@Override
	public void close() {
		this.minecraft.setScreen(parent);
	}

	public void itemSelected(Object o) {
		if (o == null) {
			selectButton.setMessage(Component.literal("Load"));
			selectButton.active = false;
		} else {
			selectButton.setMessage(Component.literal("Load"));
			selectButton.active = true;
		}
	}
}
