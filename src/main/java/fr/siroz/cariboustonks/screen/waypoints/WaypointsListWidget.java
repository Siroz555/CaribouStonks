package fr.siroz.cariboustonks.screen.waypoints;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.Arrays;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * FUTURE UPDATE -> Waypoint Settings Screen with color codes, etc.
 */
class WaypointsListWidget extends ElementListWidget<WaypointsListWidget.WaypointEntry> {

	private static final int SPACE = 6;

	private final WaypointScreen waypointScreen;
	private List<Waypoint> waypoints;

	WaypointsListWidget(
			MinecraftClient client,
			@NotNull WaypointScreen screen,
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
	protected int getScrollbarX() {
		return super.getScrollbarX();
	}

	void createWaypoint(boolean crosshair) {
		Position position = Position.of(Client.getCurrentPosition(crosshair));
		Waypoint waypoint = Waypoint.builder(position).build();

		WaypointEntry waypointEntry = new WaypointEntry(waypoint);
		int entryIndex;
		if (getSelectedOrNull() instanceof WaypointEntry selectedWaypointEntry) {
			entryIndex = children().indexOf(selectedWaypointEntry) + 1;
		} else {
			entryIndex = children().indexOf(waypointEntry) + 1;
			while (entryIndex < children().size()) {
				entryIndex++;
			}
		}

		waypoints.add(waypointEntry.waypoint);
		int newEntryIndex = addEntry(waypointEntry);
		swapEntriesOnPositions(newEntryIndex, entryIndex);
	}

	void setNewIslandType(IslandType newIslandType) {
		waypoints = waypointScreen.waypoints.get(newIslandType);
		updateEntries();
	}

	void updateButtons() {
		for (EntryListWidget.Entry<WaypointEntry> entry : children()) {
			if (entry instanceof WaypointEntry waypointEntry) {
				if (waypointEntry.enabledWidget.isChecked() != waypointEntry.waypoint.isEnabled()) {
					waypointEntry.enabledWidget.onPress(null); // le AbstractInput ne sert à rien
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

	protected class WaypointEntry extends ElementListWidget.Entry<WaypointEntry> {

		private Waypoint waypoint;
		private final List<ClickableWidget> children;
		private final CheckboxWidget enabledWidget;
		private final TextFieldWidget nameWidget;
		private final TextFieldWidget xWidget;
		private final TextFieldWidget yWidget;
		private final TextFieldWidget zWidget;
		private final CyclingButtonWidget<WaypointColors> colorWidget;
		private final CyclingButtonWidget<Waypoint.Type> typeWidget;
		private final ButtonWidget deleteWidget;

		public WaypointEntry(@NotNull Waypoint waypoint) {
			this.waypoint = waypoint;

			this.enabledWidget = CheckboxWidget.builder(Text.literal(""), client.textRenderer)
					.checked(waypoint.isEnabled())
					.tooltip(Tooltip.of(Text.literal("Click to toggle the waypoint's visibility")))
					.callback((checkbox, checked) -> waypoint.setEnabled(checked))
					.build();

			this.nameWidget = new TextFieldWidget(client.textRenderer, 65, 20, Text.literal("Name"));
			this.nameWidget.setText(waypoint.getTextOption().getText().orElse(Text.literal("")).getString());
			this.nameWidget.setTooltip(Tooltip.of(Text.literal("Click to edit the waypoint's name.")));
			this.nameWidget.setChangedListener(this::updateName);

			this.xWidget = new TextFieldWidget(client.textRenderer, 32, 20, Text.literal("X"));
			this.xWidget.setText(Integer.toString(waypoint.getPosition().x()));
			this.xWidget.setTextPredicate(this::testInt);
			this.xWidget.setChangedListener(this::updateX);

			this.yWidget = new TextFieldWidget(client.textRenderer, 32, 20, Text.literal("Y"));
			this.yWidget.setText(Integer.toString(waypoint.getPosition().y()));
			this.yWidget.setTextPredicate(this::testInt);
			this.yWidget.setChangedListener(this::updateY);

			this.zWidget = new TextFieldWidget(client.textRenderer, 32, 20, Text.literal("Z"));
			this.zWidget.setText(Integer.toString(waypoint.getPosition().z()));
			this.zWidget.setTextPredicate(this::testInt);
			this.zWidget.setChangedListener(this::updateZ);

			this.colorWidget = CyclingButtonWidget.<WaypointColors>builder(
							value -> Text.literal(value.name()).withColor(value.color.asInt()))
					.values(WaypointColors.values())
					.initially(WaypointColors.getFromWaypoint(waypoint.getColor()))
					.omitKeyText() // " : " avant la value, ques-ce que c ?
					.tooltip(value -> Tooltip.of(WaypointColors.getColoredList(value)))
					.build(0, 0, 56, 20, Text.empty(),
							(button, value) -> updateColor(value.color));

			this.typeWidget = CyclingButtonWidget.<Waypoint.Type>builder(
							value -> Text.literal(value.name()))
					.values(Waypoint.Type.values())
					.initially(waypoint.getType())
					.omitKeyText() // " : " avant la value, ques-ce que c ?
					.tooltip(value -> Tooltip.of(getWaypointTypeList(value)))
					.build(0, 0, 56, 20, Text.empty(),
							(button, value) -> updateType(value));

			this.deleteWidget = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), button -> {
				waypoints.remove(waypoint);
				removeEntry(this);
			}).width(56).build();

			this.children = List.of(enabledWidget, nameWidget, xWidget, yWidget, zWidget, colorWidget, typeWidget, deleteWidget);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getX(); //getContentX();
			int y = this.getY(); //getContentY();

			int enabledX = x + 10;
			enabledWidget.setPosition(enabledX, y + 1);

			int nameX = enabledX + enabledWidget.getWidth() + SPACE;
			nameWidget.setPosition(nameX, y);

			int afterName = nameX + nameWidget.getWidth();
			int xX = afterName + SPACE * 2;
			context.drawTextWithShadow(client.textRenderer, "X:", xX, y + 6, Colors.WHITE.asInt());
			xWidget.setPosition(xX + 9, y);

			int yX = xX + xWidget.getWidth() + (SPACE * 2);
			context.drawTextWithShadow(client.textRenderer, "Y:", yX, y + 6, Colors.WHITE.asInt());
			yWidget.setPosition(yX + 9, y);

			int zX = yX + yWidget.getWidth() + (SPACE * 2);
			context.drawTextWithShadow(client.textRenderer, "Z:", zX, y + 6, Colors.WHITE.asInt());
			zWidget.setPosition(zX + 9, y);

			int colorX = zX + zWidget.getWidth() + SPACE * 4;
			colorWidget.setPosition(colorX, y);

			int typeX = colorX + colorWidget.getWidth() + SPACE;
			typeWidget.setPosition(typeX, y);

			int deleteX = typeX + typeWidget.getWidth() + SPACE;
			deleteWidget.setPosition(deleteX, y);

			for (ClickableWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}

		private void updateName(String name) {
			// FUTURE UPDATE -> Waypoint Settings Screen with color codes, etc.
			waypoint.getTextOption().updateText(Text.literal(name));
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

		private int parseInt(@NotNull String value) throws NumberFormatException {
			return value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
		}
	}

	private enum WaypointColors {
		RED(Color.fromFormatting(Formatting.RED)),
		DARK_RED(Color.fromFormatting(Formatting.DARK_RED)),
		GREEN(Color.fromFormatting(Formatting.GREEN)),
		DARK_GREEN(Color.fromFormatting(Formatting.DARK_GREEN)),
		YELLOW(Color.fromFormatting(Formatting.YELLOW)),
		GOLD(Color.fromFormatting(Formatting.GOLD)),
		AQUA(Color.fromFormatting(Formatting.AQUA)),
		DARK_AQUA(Color.fromFormatting(Formatting.DARK_AQUA)),
		BLUE(Color.fromFormatting(Formatting.BLUE)),
		DARK_BLUE(Color.fromFormatting(Formatting.DARK_BLUE)),
		LIGHT_PURPLE(Color.fromFormatting(Formatting.LIGHT_PURPLE)),
		DARK_PURPLE(Color.fromFormatting(Formatting.DARK_PURPLE)),
		WHITE(Color.fromFormatting(Formatting.WHITE)),
		GRAY(Color.fromFormatting(Formatting.GRAY)),
		DARK_GRAY(Color.fromFormatting(Formatting.DARK_GRAY)),
		BLACK(Color.fromFormatting(Formatting.BLACK)),
		;

		final Color color;

		WaypointColors(Color color) {
			this.color = color;
		}

		public static Text getColoredList(@NotNull WaypointColors current) {
			Text result = Text.empty();
			for (WaypointColors c : WaypointColors.values()) {
				String prefix = (c == current) ? "-> " : "";
				String name = c.name();

				Text line = Text.literal(prefix)
						.append(Text.literal(name).styled(style -> style.withColor(c.color.asInt())));

				if (!result.getString().isEmpty()) {
					result = result.copy().append(Text.literal("\n")).append(line);
				} else {
					result = line;
				}
			}

			return result;
		}

		public static WaypointColors getFromWaypoint(@NotNull Color color) {
			return Arrays.stream(values())
					.filter(waypointColors -> color.equals(waypointColors.color))
					.findFirst()
					.orElse(WaypointColors.RED);
		}
	}

	private static Text getWaypointTypeList(@NotNull Waypoint.Type current) {
		Text result = Text.empty();
		for (Waypoint.Type type : Waypoint.Type.values()) {
			String prefix = (type == current) ? "-> " : "";
			String name = type.name();
			Text line = Text.literal(prefix).append(Text.literal(name).formatted(Formatting.UNDERLINE));
			if (!result.getString().isEmpty()) {
				result = result.copy().append(Text.literal("\n")).append(line);
			} else {
				result = line;
			}
		}

		return result;
	}
}
