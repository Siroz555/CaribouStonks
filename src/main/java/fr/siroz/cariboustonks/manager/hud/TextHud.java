package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A HUD for {@link Text}.
 */
public final class TextHud extends Hud {

	private final Text defaultText;
	private final Supplier<Text> textSupplier;

	public TextHud(
			@NotNull Text defaultText,
			@NotNull Supplier<Text> textSupplier,
			@NotNull HudPosition hudPosition,
			int defaultX,
			int defaultY
	) {
		super(hudPosition, defaultX, defaultY);
		this.defaultText = defaultText;
		this.textSupplier = textSupplier;
	}

	@Override
	public int width() {
		return (int) (CLIENT.textRenderer.getWidth(defaultText) * scale());
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

	private void render(Text text, @NotNull DrawContext context, int x, int y, float scale) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(scale, scale, 0);
		context.drawText(CLIENT.textRenderer, text, (int) (x / scale), (int) (y / scale), Colors.WHITE.asInt(), false);
		matrices.pop();
	}
}
