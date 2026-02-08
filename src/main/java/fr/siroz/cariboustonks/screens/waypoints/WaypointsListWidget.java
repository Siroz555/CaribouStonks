package fr.siroz.cariboustonks.screens.waypoints;

import com.mojang.blaze3d.platform.InputConstants;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.position.Position;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.util.Client;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * FUTURE UPDATE -> Waypoint Settings Screen with color codes, etc.
 */
class WaypointsListWidget extends ContainerObjectSelectionList<WaypointsListWidget.WaypointEntry> {

	private static final int SPACE = 6;

	private final WaypointScreen waypointScreen;
	private List<Waypoint> waypoints;

	WaypointsListWidget(
            Minecraft client,
            @NonNull WaypointScreen screen,
            int width,
            int height,
            int y,
            int itemHeight
	) {
		super(client, width, height, y, itemHeight);
		this.waypointScreen = screen;
		setNewIslandType(screen.currentIslandType);
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 250;
	}

	@Override
	protected int scrollBarX() {
		return super.scrollBarX();
	}

	void createWaypoint(boolean crosshair) {
		Position position = Position.of(Client.getCurrentPosition(crosshair));
		Waypoint waypoint = Waypoint.builder(position).build();

		WaypointEntry waypointEntry = new WaypointEntry(waypoint);
		int entryIndex;
		if (getSelected() instanceof WaypointEntry selectedWaypointEntry) {
			entryIndex = children().indexOf(selectedWaypointEntry) + 1;
		} else {
			entryIndex = children().indexOf(waypointEntry) + 1;
			while (entryIndex < children().size()) {
				entryIndex++;
			}
		}

		waypoints.add(waypointEntry.waypoint);
		int newEntryIndex = addEntry(waypointEntry);
		swap(newEntryIndex, entryIndex);
	}

	void setNewIslandType(IslandType newIslandType) {
		waypoints = waypointScreen.waypoints.get(newIslandType);
		updateEntries();
	}

	void updateButtons() {
		for (AbstractSelectionList.Entry<WaypointEntry> entry : children()) {
			if (entry instanceof WaypointEntry waypointEntry) {
				if (waypointEntry.enabledWidget.selected() != waypointEntry.waypoint.isEnabled()) {
					waypointEntry.enabledWidget.onPress(new InputWithModifiers() { // SIROZ-NOTE: useless
						@Override
						public @InputConstants.Value int input() {
							return 0;
						}

						@Override
						public @Modifiers int modifiers() {
							return 0;
						}
					});
				}
			}
		}
	}

	private void updateEntries() {
		clearEntries();

		if (waypoints == null) {
			return;
		}

		for (Waypoint waypoint : waypoints) {
			addEntry(new WaypointEntry(waypoint));
		}
	}

	protected class WaypointEntry extends ContainerObjectSelectionList.Entry<WaypointEntry> {

		private Waypoint waypoint;
		private final List<AbstractWidget> children;
		private final Checkbox enabledWidget;
		private final EditBox nameWidget;
		private final EditBox xWidget;
		private final EditBox yWidget;
		private final EditBox zWidget;
		private final CycleButton<WaypointColors> colorWidget;
		private final CycleButton<Waypoint.Type> typeWidget;
		private final Button deleteWidget;

		public WaypointEntry(@NonNull Waypoint waypoint) {
			this.waypoint = waypoint;

			this.enabledWidget = Checkbox.builder(Component.literal(""), minecraft.font)
					.selected(waypoint.isEnabled())
					.tooltip(Tooltip.create(Component.literal("Click to toggle the waypoint's visibility")))
					.onValueChange((checkbox, checked) -> waypoint.setEnabled(checked))
					.build();

			this.nameWidget = new EditBox(minecraft.font, 65, 20, Component.literal("Name"));
			this.nameWidget.setValue(waypoint.getTextOption().getText().orElse(Component.literal("")).getString());
			this.nameWidget.setTooltip(Tooltip.create(Component.literal("Click to edit the waypoint's name.")));
			this.nameWidget.setResponder(this::updateName);

			this.xWidget = new EditBox(minecraft.font, 32, 20, Component.literal("X"));
			this.xWidget.setValue(Integer.toString(waypoint.getPosition().x()));
			this.xWidget.setFilter(this::testInt);
			this.xWidget.setResponder(this::updateX);

			this.yWidget = new EditBox(minecraft.font, 32, 20, Component.literal("Y"));
			this.yWidget.setValue(Integer.toString(waypoint.getPosition().y()));
			this.yWidget.setFilter(this::testInt);
			this.yWidget.setResponder(this::updateY);

			this.zWidget = new EditBox(minecraft.font, 32, 20, Component.literal("Z"));
			this.zWidget.setValue(Integer.toString(waypoint.getPosition().z()));
			this.zWidget.setFilter(this::testInt);
			this.zWidget.setResponder(this::updateZ);

			this.colorWidget = CycleButton.<WaypointColors>builder(
							value -> Component.literal(value.name()).withColor(value.color.asInt()),
							() -> WaypointColors.getFromWaypoint(waypoint.getColor()))
					.withValues(WaypointColors.values())
					.displayOnlyValue() // " : " avant la value, ques-ce que c ?
					.withTooltip(value -> Tooltip.create(WaypointColors.getColoredList(value)))
					.create(0, 0, 56, 20, Component.empty(),
							(button, value) -> updateColor(value.color));

			this.typeWidget = CycleButton.<Waypoint.Type>builder(
							value -> Component.literal(value.name()),
							waypoint::getType)
					.withValues(Waypoint.Type.values())
					.displayOnlyValue() // " : " avant la value, ques-ce que c ?
					.withTooltip(value -> Tooltip.create(getWaypointTypeList(value)))
					.create(0, 0, 56, 20, Component.empty(),
							(button, value) -> updateType(value));

			this.deleteWidget = Button.builder(Component.translatable("selectServer.deleteButton"), button -> {
				waypoints.remove(waypoint);
				removeEntry(this);
			}).width(56).build();

			this.children = List.of(enabledWidget, nameWidget, xWidget, yWidget, zWidget, colorWidget, typeWidget, deleteWidget);
		}

		@Override
		public @NonNull List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public @NonNull List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getX(); //getContentX();
			int y = this.getY(); //getContentY();

			int enabledX = x + 10;
			enabledWidget.setPosition(enabledX, y + 1);

			int nameX = enabledX + enabledWidget.getWidth() + SPACE;
			nameWidget.setPosition(nameX, y);

			int afterName = nameX + nameWidget.getWidth();
			int xX = afterName + SPACE * 2;
			guiGraphics.drawString(minecraft.font, "X:", xX, y + 6, Colors.WHITE.asInt());
			xWidget.setPosition(xX + 9, y);

			int yX = xX + xWidget.getWidth() + (SPACE * 2);
			guiGraphics.drawString(minecraft.font, "Y:", yX, y + 6, Colors.WHITE.asInt());
			yWidget.setPosition(yX + 9, y);

			int zX = yX + yWidget.getWidth() + (SPACE * 2);
			guiGraphics.drawString(minecraft.font, "Z:", zX, y + 6, Colors.WHITE.asInt());
			zWidget.setPosition(zX + 9, y);

			int colorX = zX + zWidget.getWidth() + SPACE * 4;
			colorWidget.setPosition(colorX, y);

			int typeX = colorX + colorWidget.getWidth() + SPACE;
			typeWidget.setPosition(typeX, y);

			int deleteX = typeX + typeWidget.getWidth() + SPACE;
			deleteWidget.setPosition(deleteX, y);

			for (AbstractWidget child : children) {
				child.render(guiGraphics, mouseX, mouseY, deltaTicks);
			}
		}

		private void updateName(String name) {
			// FUTURE UPDATE -> Waypoint Settings Screen with color codes, etc.
			waypoint.getTextOption().updateText(Component.literal(name));
		}

		private void updateX(String xString) {
			updateInt(xString, waypoint.getPosition().x(), x -> {
				waypoint.updatePosition(Position.of(x, waypoint.getPosition().y(), waypoint.getPosition().z()));
				return waypoint;
			});
		}

		private void updateY(String yString) {
			updateInt(yString, waypoint.getPosition().y(), y -> {
				waypoint.updatePosition(Position.of(waypoint.getPosition().x(), y, waypoint.getPosition().z()));
				return waypoint;
			});
		}

		private void updateZ(String zString) {
			updateInt(zString, waypoint.getPosition().z(), z -> {
				waypoint.updatePosition(Position.of(waypoint.getPosition().x(), waypoint.getPosition().y(), z));
				return waypoint;
			});
		}

		private void updateInt(String newValueStr, int currentValue, Int2ObjectFunction<Waypoint> editedWaypoint) {
			try {
				int newValue = parseInt(newValueStr);
				if (newValue == currentValue) return;

				waypoint = editedWaypoint.apply(newValue);

				int index = waypoints.indexOf(waypoint);
				if (index >= 0) {
					waypoints.set(index, waypoint);
				}
			} catch (NumberFormatException ex) {
				CaribouStonks.LOGGER.warn("[Waypoints] Failed to parse integer: {}", newValueStr, ex);
			}
		}

		private boolean testInt(String string) {
			try {
				parseInt(string);
				return true;
			} catch (NumberFormatException ignored) {
				return false;
			}
		}

		private void updateColor(Color color) {
			if (waypoint.getColor().equals(color)) return;

			waypoint.updateColor(color);

			int index = waypoints.indexOf(waypoint);
			if (index >= 0) {
				waypoints.set(index, waypoint);
			}
		}

		private void updateType(Waypoint.Type type) {
			if (waypoint.getType() == type) return;

			waypoint.updateType(type);

			int index = waypoints.indexOf(waypoint);
			if (index >= 0) {
				waypoints.set(index, waypoint);
			}
		}

		private int parseInt(@NonNull String value) throws NumberFormatException {
			return value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
		}
	}

	private enum WaypointColors {
		RED(Color.fromFormatting(ChatFormatting.RED)),
		DARK_RED(Color.fromFormatting(ChatFormatting.DARK_RED)),
		GREEN(Color.fromFormatting(ChatFormatting.GREEN)),
		DARK_GREEN(Color.fromFormatting(ChatFormatting.DARK_GREEN)),
		YELLOW(Color.fromFormatting(ChatFormatting.YELLOW)),
		GOLD(Color.fromFormatting(ChatFormatting.GOLD)),
		AQUA(Color.fromFormatting(ChatFormatting.AQUA)),
		DARK_AQUA(Color.fromFormatting(ChatFormatting.DARK_AQUA)),
		BLUE(Color.fromFormatting(ChatFormatting.BLUE)),
		DARK_BLUE(Color.fromFormatting(ChatFormatting.DARK_BLUE)),
		LIGHT_PURPLE(Color.fromFormatting(ChatFormatting.LIGHT_PURPLE)),
		DARK_PURPLE(Color.fromFormatting(ChatFormatting.DARK_PURPLE)),
		WHITE(Color.fromFormatting(ChatFormatting.WHITE)),
		GRAY(Color.fromFormatting(ChatFormatting.GRAY)),
		DARK_GRAY(Color.fromFormatting(ChatFormatting.DARK_GRAY)),
		BLACK(Color.fromFormatting(ChatFormatting.BLACK)),
		;

		final Color color;

		WaypointColors(Color color) {
			this.color = color;
		}

		public static Component getColoredList(@NonNull WaypointColors current) {
			Component result = Component.empty();
			for (WaypointColors c : WaypointColors.values()) {
				String prefix = (c == current) ? "-> " : "";
				String name = c.name();

				Component line = Component.literal(prefix)
						.append(Component.literal(name).withStyle(style -> style.withColor(c.color.asInt())));

				if (!result.getString().isEmpty()) {
					result = result.copy().append(Component.literal("\n")).append(line);
				} else {
					result = line;
				}
			}

			return result;
		}

		public static WaypointColors getFromWaypoint(@NonNull Color color) {
			return Arrays.stream(values())
					.filter(waypointColors -> color.equals(waypointColors.color))
					.findFirst()
					.orElse(WaypointColors.RED);
		}
	}

	private static Component getWaypointTypeList(Waypoint.@NonNull Type current) {
		Component result = Component.empty();
		for (Waypoint.Type type : Waypoint.Type.values()) {
			String prefix = (type == current) ? "-> " : "";
			String name = type.name();
			Component line = Component.literal(prefix).append(Component.literal(name).withStyle(ChatFormatting.UNDERLINE));
			if (!result.getString().isEmpty()) {
				result = result.copy().append(Component.literal("\n")).append(line);
			} else {
				result = line;
			}
		}

		return result;
	}
}
