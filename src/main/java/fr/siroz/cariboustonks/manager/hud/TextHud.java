package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A HUD for {@link Component}.
 */
public final class TextHud extends Hud {

	private final Component defaultText;
	private final Supplier<Component> textSupplier;

	public TextHud(
			@NotNull Component defaultText,
			@NotNull Supplier<Component> textSupplier,
			@NotNull HudConfig hudConfig,
			int defaultX,
			int defaultY
	) {
		super(() -> true, hudConfig, defaultX, defaultY);
		this.defaultText = defaultText;
		this.textSupplier = textSupplier;
	}

	@Override
	public int width() {
		return (int) (CLIENT.font.width(defaultText) * scale());
	}

	@Override
	public int height() {
		return (int) (CLIENT.font.lineHeight * scale());
	}

	@Override
	public void renderScreen(GuiGraphics guiGraphics) {
		render(defaultText, guiGraphics, x(), y(), scale());
	}

	@Override
	public void renderHud(GuiGraphics guiGraphics, DeltaTracker tickCounter) {
		Component text = textSupplier.get();
		if (shouldRender() && !text.getString().isEmpty()) {
			render(text, guiGraphics, hudConfig.x(), hudConfig.y(), hudConfig.scale());
		}
	}

	private void render(Component text, @NotNull GuiGraphics guiGraphics, int x, int y, float scale) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.drawString(CLIENT.font, text, (int) (x / scale), (int) (y / scale), Colors.WHITE.asInt(), false);
		guiGraphics.pose().popMatrix();
	}
}
