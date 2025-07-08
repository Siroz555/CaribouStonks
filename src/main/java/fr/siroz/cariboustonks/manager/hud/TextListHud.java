package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A HUD for a {@code list} of {@link Text}.
 */
@ApiStatus.Experimental
public final class TextListHud extends Hud {

	private final List<Text> defaultText;
	private final Supplier<List<Text>> textSupplier;

	public TextListHud(
			@NotNull List<Text> defaultText,
			@NotNull Supplier<List<Text>> textSupplier,
			@NotNull HudPosition hudPosition,
			int defaultX,
			int defaultY
	) {
		super(hudPosition, defaultX, defaultY);
		if (defaultText.isEmpty()) {
			throw new IllegalArgumentException("The text list cannot be empty");
		}

		this.defaultText = defaultText;
		this.textSupplier = textSupplier;
	}

	@Override
	public int width() {
		return (int) (CLIENT.textRenderer.getWidth(defaultText.getFirst()) * scale());
	}

	@Override
	public int height() {
		return (int) (CLIENT.textRenderer.fontHeight * scale());
	}

	@Override
	public void renderScreen(DrawContext context) {
		render(defaultText, context, x(), y(), scale());
	}

	@Override
	public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		if (shouldRender()) {
			render(textSupplier.get(), context, hudPosition.x(), hudPosition.y(), hudPosition.scale());
		}
	}

	private void render(@NotNull List<Text> text, @NotNull DrawContext context, int x, int y, float scale) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(scale, scale, 0);

		int baseY = (int) (y / scale);
		int offset = 0;
		for (int i = 0; i < text.size(); ++i) {
			int yLine = baseY + i * CLIENT.textRenderer.fontHeight + offset;
			context.drawText(CLIENT.textRenderer, text.get(i), (int) (x / scale), yLine, Colors.WHITE.asInt(), false);
			offset += 4;
		}

		matrices.pop();
	}
}
