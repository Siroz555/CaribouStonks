package fr.siroz.cariboustonks.util.render.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

/**
 * Credits to the Skyblocker Team (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>)
 */
public class DropdownWidget<T> extends AbstractContainerWidget {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final int ENTRY_HEIGHT = 15;
	private static final int HEADER_HEIGHT = ENTRY_HEIGHT + 4;
	private static final int BACKGROUND_COLOR = Colors.BLACK.withAlpha(0.5f).asInt();

	private final int maxHeight;
	protected final List<T> entries;
	protected final Consumer<T> selectCallback;
	protected T selected;
	private final DropdownList dropdownList;

	protected T prevSelected;
	protected boolean open;

	public DropdownWidget(
			int x,
			int y,
			int width,
			int maxHeight,
			@NonNull List<T> entries,
			@NonNull Consumer<T> selectCallback,
			T selected
	) {
		super(x, y, width, HEADER_HEIGHT, Component.empty());
		this.maxHeight = maxHeight;
		this.entries = entries;
		this.selectCallback = selectCallback;
		this.selected = selected;
		this.dropdownList = new DropdownList(CLIENT, x + 1, y + HEADER_HEIGHT, width - 2, maxHeight - HEADER_HEIGHT);

		for (T element : entries) {
			this.dropdownList.addEntry(new Entry(element));
		}
	}

	@Override
	public @NonNull List<? extends GuiEventListener> children() {
		return List.of(dropdownList);
	}

	@Override
	protected void renderWidget(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
		dropdownList.visible = open;
		dropdownList.render(context, mouseX, mouseY, delta);

		context.fill(getX(), getY(), getRight(), getY() + HEADER_HEIGHT + 1, BACKGROUND_COLOR);
		GuiRenderer.drawBorder(context, getX(), getY(), getWidth(), HEADER_HEIGHT + 1, -1);
		context.drawString(CLIENT.font, ">", getX() + 4, getY() + 6, Colors.WHITE.asInt(), true);
		context.drawString(CLIENT.font, selected.toString(), getX() + 12, getY() + 6, Colors.WHITE.asInt(), true);
		if (isMouseOver(mouseX, mouseY)) {
			context.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) {
	}

	private void setOpen(boolean open) {
		this.open = open;

		if (this.open) {
			setHeight(maxHeight);
			dropdownList.setHeight(Math.min(entries.size() * ENTRY_HEIGHT + 4, maxHeight - HEADER_HEIGHT));
		} else {
			setHeight(HEADER_HEIGHT);
		}
	}

	protected void select(T entry) {
		selected = entry;
		setOpen(false);

		if (selected != prevSelected) {
			selectCallback.accept(entry);
			prevSelected = selected;
		}
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		dropdownList.setX(getX() + 1);
		dropdownList.refreshScrollAmount();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		dropdownList.setY(getY() + HEADER_HEIGHT);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		dropdownList.setWidth(getWidth() - 2);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		dropdownList.setHeight(height - HEADER_HEIGHT);
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
		if (!visible) return false;

		if (getX() <= click.x() && click.x() < getX() + getWidth() && getY() <= click.y() && click.y() < getY() + HEADER_HEIGHT) {
			setOpen(!open);
			playDownSound(CLIENT.getSoundManager());
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	protected int contentHeight() {
		return getHeight();
	}

	@Override
	protected double scrollRate() {
		return 0;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;

		if (this.getChildAt(mouseX, mouseY)
				.filter(e -> e.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
				.isPresent()) {
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private class DropdownList extends ContainerObjectSelectionList<Entry> {

		protected DropdownList(Minecraft minecraftClient, int x, int y, int width, int height) {
			super(minecraftClient, width, height, y, ENTRY_HEIGHT);
			setX(x);
		}

		@Override
		protected int addEntry(DropdownWidget<T>.Entry entry) {
			return super.addEntry(entry);
		}

		@Override
		public int getRowLeft() {
			return getX() + 2;
		}

		@Override
		public int getRowWidth() {
			return getWidth() - 5;
		}

		@Override
		protected void renderScrollbar(@NonNull GuiGraphics context, int mouseX, int mouseY) {
			if (this.scrollbarVisible()) {
				int x1 = this.scrollBarX();
				int heightY = this.scrollerHeight();
				int y1 = this.scrollBarY();
				context.fill(x1, y1 + 1, x1 + 2, y1 + heightY, -1);
			}
		}

		@Override
		protected int scrollBarX() {
			return getRowLeft() + getRowWidth();
		}

		@Override
		public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
			if (!visible) return false;
			return super.mouseClicked(click, doubled);
		}

		@Override
		public boolean mouseReleased(@NonNull MouseButtonEvent click) {
			if (!visible) return false;
			return super.mouseReleased(click);
		}

		@Override
		public boolean mouseDragged(@NonNull MouseButtonEvent click, double offsetX, double offsetY) {
			if (!visible) return false;
			return super.mouseDragged(click, offsetX, offsetY);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (!visible) return false;
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		@Override
		protected void renderListSeparators(@NonNull GuiGraphics context) {
		}

		@Override
		protected void renderListBackground(@NonNull GuiGraphics context) {
			context.fill(getX(), getY(), getRight(), getBottom(), BACKGROUND_COLOR);
			GuiRenderer.drawBorder(context, getX(), getY(), getWidth(), getHeight(), -1);
		}

		@Override
		protected void enableScissor(@NonNull GuiGraphics context) {
			context.enableScissor(this.getX(), this.getY() + 1, this.getRight(), this.getBottom() - 1);
		}
	}

	private class Entry extends ContainerObjectSelectionList.Entry<Entry> {

		private final T entry;

		protected Entry(T element) {
			this.entry = element;
		}

		@Override
		public @NonNull List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public @NonNull List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public void renderContent(@NonNull GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.textRenderer(GuiGraphics.HoveredTextEffects.NONE).acceptScrollingWithDefaultCenter(
					Component.literal(entry.toString()).withStyle(Style.EMPTY.withUnderlined(hovered)),
					this.getX() + 10, this.getX() + this.getWidth(), this.getY(), this.getY() + 11
			);

			if (selected == this.entry) {
				context.drawString(CLIENT.font, "->", getX() + 1, getY() + 2, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
			select(entry);
			return true;
		}
	}
}
