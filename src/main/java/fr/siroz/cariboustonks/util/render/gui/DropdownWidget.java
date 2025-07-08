package fr.siroz.cariboustonks.util.render.gui;

import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
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
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0, 0, 100);

		dropdownList.visible = open;
		dropdownList.render(context, mouseX, mouseY, delta);

		context.fill(getX(), getY(), getRight(), getY() + HEADER_HEIGHT + 1, BACKGROUND_COLOR);
		context.drawBorder(getX(), getY(), getWidth(), HEADER_HEIGHT + 1, -1);

		drawScrollableText(context, CLIENT.textRenderer, Text.literal(selected.toString()),
				getX() + 2,
				getY() + 2,
				getRight() - 2,
				getY() + HEADER_HEIGHT - 2,
				-1);

		matrices.pop();
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!visible) return false;

		if (getX() <= mouseX && mouseX < getX() + getWidth() && getY() <= mouseY && mouseY < getY() + HEADER_HEIGHT) {
			setOpen(!open);
			playDownSound(CLIENT.getSoundManager());
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
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
		protected void drawScrollbar(DrawContext context) {
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
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!visible) return false;
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			if (!visible) return false;
			return super.mouseReleased(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (!visible) return false;
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
			context.drawBorder(getX(), getY(), getWidth(), getHeight(), -1);
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
		public void render(DrawContext context, int index,
						   int y, int x,
						   int entryWidth, int entryHeight,
						   int mouseX, int mouseY,
						   boolean hovered, float tickDelta
		) {
			drawScrollableText(context, CLIENT.textRenderer,
					Text.literal(entry.toString()).fillStyle(Style.EMPTY.withUnderline(hovered)),
					x + 10, y, x + entryWidth, y + 11, -1);

			if (selected == this.entry) {
				context.drawTextWithShadow(CLIENT.textRenderer, "->", x + 1, y + 2, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			select(entry);
			return true;
		}
	}
}
