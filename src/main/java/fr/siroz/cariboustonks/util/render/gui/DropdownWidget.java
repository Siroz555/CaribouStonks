package fr.siroz.cariboustonks.util.render.gui;

import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Credits to the Skyblocker Team (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>)
 */
public class DropdownWidget<T> extends ContainerWidget {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

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
			@NotNull List<T> entries,
			@NotNull Consumer<T> selectCallback,
			T selected
	) {
		super(x, y, width, HEADER_HEIGHT, Text.empty());
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
	public List<? extends Element> children() {
		return List.of(dropdownList);
	}

	@Override
	protected void renderWidget(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
		dropdownList.visible = open;
		dropdownList.render(context, mouseX, mouseY, delta);

		context.fill(getX(), getY(), getRight(), getY() + HEADER_HEIGHT + 1, BACKGROUND_COLOR);
		GuiRenderer.drawBorder(context, getX(), getY(), getWidth(), HEADER_HEIGHT + 1, -1);
		context.drawText(CLIENT.textRenderer, ">", getX() + 4, getY() + 6, Colors.WHITE.asInt(), true);
		context.drawText(CLIENT.textRenderer, selected.toString(), getX() + 12, getY() + 6, Colors.WHITE.asInt(), true);
		if (isMouseOver(mouseX, mouseY)) {
			context.setCursor(StandardCursors.POINTING_HAND);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
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
		dropdownList.refreshScroll();
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
	public boolean mouseClicked(Click click, boolean doubled) {
		if (!visible) return false;

		if (getX() <= click.x() && click.x() < getX() + getWidth() && getY() <= click.y() && click.y() < getY() + HEADER_HEIGHT) {
			setOpen(!open);
			playDownSound(CLIENT.getSoundManager());
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return getHeight();
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;

		if (this.hoveredElement(mouseX, mouseY)
				.filter(e -> e.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
				.isPresent()) {
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private class DropdownList extends ElementListWidget<Entry> {

		protected DropdownList(MinecraftClient minecraftClient, int x, int y, int width, int height) {
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
		protected void drawScrollbar(DrawContext context, int mouseX, int mouseY) {
			if (this.overflows()) {
				int x1 = this.getScrollbarX();
				int heightY = this.getScrollbarThumbHeight();
				int y1 = this.getScrollbarThumbY();
				context.fill(x1, y1 + 1, x1 + 2, y1 + heightY, -1);
			}
		}

		@Override
		protected int getScrollbarX() {
			return getRowLeft() + getRowWidth();
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			if (!visible) return false;
			return super.mouseClicked(click, doubled);
		}

		@Override
		public boolean mouseReleased(Click click) {
			if (!visible) return false;
			return super.mouseReleased(click);
		}

		@Override
		public boolean mouseDragged(Click click, double offsetX, double offsetY) {
			if (!visible) return false;
			return super.mouseDragged(click, offsetX, offsetY);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (!visible) return false;
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		@Override
		protected void drawHeaderAndFooterSeparators(DrawContext context) {
		}

		@Override
		protected void drawMenuListBackground(@NotNull DrawContext context) {
			context.fill(getX(), getY(), getRight(), getBottom(), BACKGROUND_COLOR);
			GuiRenderer.drawBorder(context, getX(), getY(), getWidth(), getHeight(), -1);
		}

		@Override
		protected void enableScissor(@NotNull DrawContext context) {
			context.enableScissor(this.getX(), this.getY() + 1, this.getRight(), this.getBottom() - 1);
		}
	}

	private class Entry extends ElementListWidget.Entry<Entry> {

		private final T entry;

		protected Entry(T element) {
			this.entry = element;
		}

		@Contract(pure = true)
		@Override
		public @NotNull @Unmodifiable List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Contract(pure = true)
		@Override
		public @NotNull @Unmodifiable List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			GuiRenderer.drawScrollableText(context, CLIENT.textRenderer,
					Text.literal(entry.toString()).fillStyle(Style.EMPTY.withUnderline(hovered)),
					getX() + 10, getY(), getX() + getContentWidth(), getY() + 11, -1);

			if (selected == this.entry) {
				context.drawTextWithShadow(CLIENT.textRenderer, "->", getX() + 1, getY() + 2, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			select(entry);
			return true;
		}
	}
}
