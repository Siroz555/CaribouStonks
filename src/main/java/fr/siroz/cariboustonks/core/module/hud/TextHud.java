package fr.siroz.cariboustonks.core.module.hud;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.module.color.Colors;
import java.util.function.Supplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * A HUD for {@link Component}.
 */
public final class TextHud extends Hud {

	private final Component defaultText;
	private final Supplier<Component> textSupplier;

	public TextHud(
			@NonNull Component defaultText,
			@NonNull Supplier<Component> textSupplier,
			@NonNull HudConfig hudConfig,
			int defaultOffsetX,
			int defaultOffsetY
	) {
		super(() -> true, hudConfig, defaultOffsetX, defaultOffsetY);
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
	public void renderScreen(GuiGraphicsExtractor guiGraphics) {
		if (hudConfig.shouldRender()) {
			render(defaultText, guiGraphics, x(), y(), scale());
		}
	}

	@Override
	public void renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker tickCounter) {
		Component text = textSupplier.get();
		if (shouldRender() && !text.getString().isEmpty()) {
			render(text, guiGraphics, configX(), configY(), hudConfig.scale());
		}
	}

	private void render(Component text, @NonNull GuiGraphicsExtractor guiGraphics, int x, int y, float scale) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.text(CLIENT.font, text, (int) (x / scale), (int) (y / scale), Colors.WHITE.asInt(), useShadow());
		guiGraphics.pose().popMatrix();
	}

	private boolean useShadow() {
		return ConfigManager.getConfig().uiAndVisuals.shadowTextHud;
	}
}
