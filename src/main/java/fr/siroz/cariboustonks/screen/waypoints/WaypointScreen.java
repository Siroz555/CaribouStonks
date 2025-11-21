package fr.siroz.cariboustonks.screen.waypoints;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.waypoints.WaypointFeature;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.gui.DropdownWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WaypointScreen extends CaribousStonksScreen {

	@Nullable
	private final Screen parent;
	private final WaypointFeature waypointFeature;
	protected IslandType currentIslandType;
	protected final Map<IslandType, List<Waypoint>> waypoints;

	private WaypointsListWidget waypointsListWidget;
	private DropdownWidget<IslandType> islandDropdownWidget;

	private WaypointScreen(@Nullable Screen parent) {
		super(Component.literal("Waypoints").withStyle(ChatFormatting.BOLD));
		this.parent = parent;
		this.currentIslandType = SkyBlockAPI.getIsland();
		this.waypointFeature = CaribouStonks.features().getFeature(WaypointFeature.class);
		this.waypoints = this.waypointFeature.getWaypoints();
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull WaypointScreen create(@Nullable Screen parent) {
		return new WaypointScreen(parent);
	}

	@Override
	protected void onInit() {
		waypointsListWidget = this.addRenderableWidget(new WaypointsListWidget(
                minecraft, this, width, height - 120, 32, 24));
		islandDropdownWidget = this.addRenderableWidget(new DropdownWidget<>(
				width - 160, 8, 150, height - 8,
				Arrays.asList(IslandType.VALUES), this::onIslandChanged, currentIslandType));

		GridLayout grid = new GridLayout();
		grid.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);

		GridLayout.RowHelper adder = grid.createRowHelper(2);
		adder.addChild(Button.builder(Component.literal("Add (At Crosshair)"),
				button -> waypointsListWidget.createWaypoint(true)).build());

		adder.addChild(Button.builder(Component.literal("Add (Your position)"),
				button -> waypointsListWidget.createWaypoint(false)).build());

		adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			saveWaypoints();
			close();
		}).width(310).build(), 2);

		grid.arrangeElements();
		FrameLayout.centerInRectangle(grid, 0, this.height - 64, this.width, 64);
		grid.visitWidgets(this::addRenderableWidget);
		updateButtons();
	}

	@Override
	public void onRender(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.onRender(context, mouseX, mouseY, delta);
		context.drawCenteredString(this.font, this.title, this.width / 2, 16, Colors.WHITE.asInt());
	}

	@Override
	public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
		if (islandDropdownWidget.mouseClicked(click, doubled)) {
			return true;
		}

		boolean mouseClicked = super.onMouseClicked(click, doubled);
		updateButtons();
		return mouseClicked;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (islandDropdownWidget.isMouseOver(mouseX, mouseY)
				&& islandDropdownWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void saveWaypoints() {
		waypointFeature.updateWaypoints(waypoints);
		waypointFeature.saveWaypoints(minecraft);
	}

	private void onIslandChanged(IslandType islandType) {
		currentIslandType = islandType;
		waypointsListWidget.setNewIslandType(islandType);
	}

	private void updateButtons() {
		waypointsListWidget.updateButtons();
	}

	@Override
	public void close() {
		assert minecraft != null;
		minecraft.setScreen(parent);
	}
}
