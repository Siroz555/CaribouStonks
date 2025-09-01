package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import fr.siroz.cariboustonks.manager.hud.element.HudIconLine;
import fr.siroz.cariboustonks.manager.hud.element.HudTextLine;
import fr.siroz.cariboustonks.manager.hud.element.HudTableRow;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A HUD for a list of {@link HudElement}.
 */
public final class MultiElementHud extends Hud {

	private static final int SPACING = 2;

	private final List<HudTextLine> defaultText;
	private final Supplier<List<? extends HudElement>> elementSupplier;

	/**
	 * Create a new {@link MultiElementHud} instance.
	 *
	 * @param enabledSupplier the enabled state supplier
	 * @param defaultText     the default list of {@link HudTextLine}
	 * @param elementSupplier the supplier of element list
	 * @param hudConfig       the {@link HudConfig} from the config file
	 * @param defaultX        the default X
	 * @param defaultY        the default Y
	 */
	public MultiElementHud(
			@NotNull Supplier<Boolean> enabledSupplier,
			@NotNull List<HudTextLine> defaultText,
			@NotNull Supplier<List<? extends HudElement>> elementSupplier,
			@NotNull HudConfig hudConfig,
			int defaultX,
			int defaultY
	) {
		super(enabledSupplier, hudConfig, defaultX, defaultY);
		if (defaultText.isEmpty()) {
			throw new IllegalArgumentException("The text list cannot be empty");
		}

		this.defaultText = defaultText;
		this.elementSupplier = elementSupplier;
	}

	@Override
	public int width() {
		int maxWidth = defaultText.stream()
				.mapToInt(line -> CLIENT.textRenderer.getWidth(line.text()))
				.max()
				.orElse(0);
		return (int) (maxWidth * scale());
	}

	@Override
	public int height() {
		int lineHeight = (int) (CLIENT.textRenderer.fontHeight * scale());
		return defaultText.stream()
				.mapToInt(t -> lineHeight + (t.spaceAfter() ? SPACING : 0))
				.sum() - SPACING;
	}

	@Override
	public void renderScreen(DrawContext context) {
		render(defaultText, context, x(), y(), scale());
	}

	@Override
	public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		// SIROZ-NOTE: un try-catch pour le rendu hud hors screen avec le système de crash en préparation
		if (shouldRender()) {
			render(elementSupplier.get(), context, hudConfig.x(), hudConfig.y(), hudConfig.scale());
		}
	}

	private void render(@NotNull List<? extends HudElement> elements, @NotNull DrawContext context, int x, int y, float scale) {
		context.getMatrices().pushMatrix();
		context.getMatrices().scale(scale, scale);

		// Récupère le nombre max de columns uniquement pour les HudTableRow
		// SIROZ-NOTE: Supprimer les cells pour les elements qui en on pas besoin
		int maxCols = elements.stream()
				.filter(e -> e instanceof HudTableRow)
				.mapToInt(r -> r.getCells().length)
				.max()
				.orElse(0);

		// Pour chaque colonne, rechercher la largeur max
		int[] colWidth = new int[maxCols];
		for (HudElement element : elements) {
			if (element instanceof HudTableRow row) {
				Text[] cells = row.getCells();
				for (int i = 0; i < cells.length; i++) {
					int w = CLIENT.textRenderer.getWidth(cells[i]);
					if (w > colWidth[i]) {
						colWidth[i] = w;
					}
				}
			}
		}

		int baseY = (int) (y / scale);
		int offset = 0;
		int cellPadding = (int) (SPACING * scale);
		int lineHeight = CLIENT.textRenderer.fontHeight;

		for (HudElement element : elements) {
			if (element instanceof HudTableRow row) {
				// Déssine chaque cell a son tab stop
				int cellX = (int) (x / scale);
				Text[] cells = row.getCells();
				for (int i = 0; i < cells.length; i++) {
					context.drawText(CLIENT.textRenderer, cells[i], cellX, baseY + offset, Colors.WHITE.asInt(), false);
					cellX += colWidth[i] + cellPadding;
				}
			} else if (element instanceof HudTextLine line) {
				context.drawText(CLIENT.textRenderer, line.text(), (int) (x / scale), baseY + offset, Colors.WHITE.asInt(), false);
			} else if (element instanceof HudIconLine icon) {
				context.drawItem(icon.stack(), (int) (x / scale), baseY + offset);
				context.drawText(CLIENT.textRenderer, icon.text(), (int) (x / scale) + 18, baseY + offset + 4, Colors.WHITE.asInt(), false);
			}

			// Avance verticalement : hauteur de ligne + éventuel interligne / Icon
			offset += (element instanceof HudIconLine ? 16 : lineHeight);
			if (element.hasSpaceAfter()) {
				offset += (int) (SPACING * scale);
			}
		}

		context.getMatrices().popMatrix();
	}
}
