package fr.siroz.cariboustonks.screen.waypoints;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.waypoints.WaypointFeature;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.gui.DropdownWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
		super(Text.literal("Waypoints").formatted(Formatting.BOLD));
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
		waypointsListWidget = this.addDrawableChild(new WaypointsListWidget(
				client, this, width, height - 120, 32, 24));
		islandDropdownWidget = this.addDrawableChild(new DropdownWidget<>(
				width - 160, 8, 150, height - 8,
				Arrays.asList(IslandType.VALUES), this::onIslandChanged, currentIslandType));

		GridWidget grid = new GridWidget();
		grid.getMainPositioner().marginX(5).marginY(2);

		GridWidget.Adder adder = grid.createAdder(2);
		adder.add(ButtonWidget.builder(Text.literal("Add (At Crosshair)"),
				button -> waypointsListWidget.createWaypoint(true)).build());

		adder.add(ButtonWidget.builder(Text.literal("Add (Your position)"),
				button -> waypointsListWidget.createWaypoint(false)).build());

		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			saveWaypoints();
			onClose();
		}).width(310).build(), 2);

		grid.refreshPositions();
		SimplePositioningWidget.setPos(grid, 0, this.height - 64, this.width, 64);
		grid.forEachChild(this::addDrawableChild);
		updateButtons();
	}

	@Override
	public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
		super.onRender(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, Colors.WHITE.asInt());
	}

	@Override
	public boolean onMouseClicked(Click click, boolean doubled) {
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
		waypointFeature.saveWaypoints(client);
	}

	private void onIslandChanged(IslandType islandType) {
		currentIslandType = islandType;
		waypointsListWidget.setNewIslandType(islandType);
	}

	private void updateButtons() {
		waypointsListWidget.updateButtons();
	}

	@Override
	public void onClose() {
		assert client != null;
		client.setScreen(parent);
	}
}
