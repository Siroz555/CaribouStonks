package fr.siroz.cariboustonks.core.module.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.mod.crash.CrashType;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.module.hud.element.HudIconLine;
import fr.siroz.cariboustonks.core.module.hud.element.HudTableRow;
import fr.siroz.cariboustonks.core.module.hud.element.HudTextLine;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * A HUD for a list of {@link HudElement}.
 */
public final class MultiElementHud extends Hud {
	private static final int SPACING = 2;

	private final String name;
	private final Consumer<HudElementBuilder> preview;
	private final Consumer<HudElementBuilder> content;
	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	private int cachedRawWidth;
	private int cachedRawHeight;

	/**
	 * Create a new {@link MultiElementHud} instance.
	 *
	 * @param name            the unique name (id) if the HUD
	 * @param enabledSupplier the enabled state supplier
	 * @param preview         the consumer of the element builder for PREVIEW rendering
	 * @param content         the consumer of the element builder for CONTENT rendering
	 * @param hudConfig       the {@link HudConfig} from the config file
	 * @param defaultOffsetX  the default X
	 * @param defaultOffsetY  the default Y
	 */
	public MultiElementHud(
			@NonNull String name,
			@NonNull Supplier<Boolean> enabledSupplier,
			@NonNull Consumer<HudElementBuilder> preview,
			@NonNull Consumer<HudElementBuilder> content,
			@NonNull HudConfig hudConfig,
			int defaultOffsetX,
			int defaultOffsetY
	) {
		super(enabledSupplier, hudConfig, defaultOffsetX, defaultOffsetY);
		this.name = name;
		this.preview = preview;
		this.content = content;
	}

	@Override
	public int width() {
		return (int) (cachedRawWidth * scale());
	}

	@Override
	public int height() {
		return (int) (cachedRawHeight * scale());
	}

	@Override
	public void renderScreen(GuiGraphicsExtractor guiGraphics) {
		if (!hudConfig.shouldRender()) return;

		hudBuilder.clear();
		preview.accept(hudBuilder);
		List<HudElement> elements = hudBuilder.build();
		if (elements.isEmpty()) return;

		recalcDimensions(elements);
		render(elements, guiGraphics, x(), y(), scale());
	}

	@Override
	public void renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker tickCounter) {
		if (!shouldRender()) return;

		hudBuilder.clear();
		content.accept(hudBuilder);
		List<HudElement> elements = hudBuilder.build();
		if (elements.isEmpty()) return;

		recalcDimensions(elements);

		try {
			render(elements, guiGraphics, configX(), configY(), hudConfig.scale());
		} catch (Throwable throwable) {
			CaribouStonks.mod().getCrashManager().reportCrash(CrashType.HUD, name, name, "renderHud", throwable);
		}
	}

	private void recalcDimensions(@NonNull List<? extends HudElement> elements) {

		int[] colWidths = computeColumnWidths(elements);
		int lineHeight = CLIENT.font.lineHeight;

		int maxWidth = 0;
		int totalHeight = 0;

		for (int i = 0; i < elements.size(); i++) {
			HudElement element = elements.get(i);

			int w = elementRawWidth(element, colWidths);
			if (w > maxWidth) maxWidth = w;

			totalHeight += (element instanceof HudIconLine) ? 16 : lineHeight;
			// L'espacement de fin est exclu.
			if (i < elements.size() - 1 && element.hasSpaceAfter()) {
				totalHeight += SPACING;
			}
		}

		cachedRawWidth = maxWidth;
		cachedRawHeight = totalHeight;
	}

	private int[] computeColumnWidths(@NonNull List<? extends HudElement> elements) {
		int maxCols = 0;
		for (HudElement element : elements) {
			if (element instanceof HudTableRow row) {
				maxCols = Math.max(maxCols, row.getCells().length);
			}
		}
		int[] widths = new int[maxCols];
		for (HudElement element : elements) {
			if (element instanceof HudTableRow row) {
				Component[] cells = row.getCells();
				for (int i = 0; i < cells.length; i++) {
					widths[i] = Math.max(widths[i], CLIENT.font.width(cells[i]));
				}
			}
		}
		return widths;
	}

	private int elementRawWidth(@NonNull HudElement element, int @NonNull [] colWidths) {
		// Renvoie la largeur en pixels brute (non mise à l'échelle) d'un élément unique,
		// en utilisant des largeurs de colonnes pré-calculées pour HudTableRow.
		switch (element) {
			case HudTextLine line -> {
				return CLIENT.font.width(line.text());
			}
			case HudTableRow row -> {
				int w = 0;
				Component[] cells = row.getCells();
				for (int i = 0; i < cells.length; i++) {
					w += colWidths[i];
					if (i < cells.length - 1) w += SPACING;
				}
				return w;
			}
			case HudIconLine icon -> {
				return 16 + 2 + CLIENT.font.width(icon.text()); // icon + gap + text
			}
			default -> {
			}
		}
		return 0;
	}

	private void render(@NonNull List<? extends HudElement> elements, @NonNull GuiGraphicsExtractor guiGraphics, int x, int y, float scale) {
		int[] colWidths = computeColumnWidths(elements);

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().scale(scale, scale);

		int scaledX = (int) (x / scale);
		int scaledY = (int) (y / scale);
		int lineHeight = CLIENT.font.lineHeight;
		boolean shadow = ConfigManager.getConfig().uiAndVisuals.shadowTextHud;
		int white = Colors.WHITE.asInt();

		int offset = 0;
		for (int i = 0; i < elements.size(); i++) {
			HudElement element = elements.get(i);
			int elementY = scaledY + offset;

			if (element instanceof HudTableRow row) {
				int cellX = scaledX;
				Component[] cells = row.getCells();
				for (int c = 0; c < cells.length; c++) {
					guiGraphics.text(CLIENT.font, cells[c], cellX, elementY, white, shadow);
					cellX += colWidths[c] + SPACING;
				}
			} else if (element instanceof HudTextLine line) {
				guiGraphics.text(CLIENT.font, line.text(), scaledX, elementY, white, shadow);
			} else if (element instanceof HudIconLine icon) {
				guiGraphics.item(icon.stack(), scaledX, elementY);
				guiGraphics.text(CLIENT.font, icon.text(), scaledX + 18, elementY + 4, white, shadow);
			}

			offset += (element instanceof HudIconLine) ? 16 : lineHeight;
			// Même chose que le recalcDim, l'espacement de fin est exclu.
			if (i < elements.size() - 1 && element.hasSpaceAfter()) {
				offset += SPACING;
			}
		}

		guiGraphics.pose().popMatrix();
	}
}
