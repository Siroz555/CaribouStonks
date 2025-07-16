package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Calculator;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class REISearchBarCalculatorFeature extends Feature {

	private String lastSearchBarInput = "";
	private String lastCalculatorResult = null;

	public REISearchBarCalculatorFeature() {
		ScreenEvents.BEFORE_INIT.register(this::onScreenBeforeInit);
	}

	@Override
	public boolean isEnabled() {
		return ConfigManager.getConfig().misc.compatibility.reiSearchBarCalculator
				&& FabricLoader.getInstance().isModLoaded("roughlyenoughitems");
	}

	@EventHandler(event = "ScreenEvents.BEFORE_INIT")
	private void onScreenBeforeInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
		if (isEnabled() && screen instanceof HandledScreen<?> handledScreen) {
			ScreenEvents.afterRender(screen).register((_screen, context, mouseX, mouseY, tickDelta) -> {
				TextField searchBarField = REIRuntime.getInstance().getSearchTextField();
				if (searchBarField == null) {
					return;
				}

				String searchBarText = searchBarField.getText();
				if (searchBarText.isBlank() || !searchBarText.matches(".*\\d.*")) {
					return;
				}

				Text calculatorTextResult = getCalculatorTextResult(searchBarText);
				if (calculatorTextResult.getString().contains("=")) {
					draw(context, handledScreen, calculatorTextResult);
				}
			});
		}
	}

	private @NotNull Text getCalculatorTextResult(String searchBarFieldText) {
		String result = calculate(searchBarFieldText);
		return result == null ? Text.empty() : Text.literal(searchBarFieldText).formatted(Formatting.GREEN)
				.append(Text.literal(" = ").formatted(Formatting.YELLOW))
				.append(Text.literal(result).formatted(Formatting.GREEN));
	}

	private @Nullable String calculate(String searchBarInput) {
		if (!lastSearchBarInput.equals(searchBarInput)) {
			lastSearchBarInput = searchBarInput;

			try {
				double result = Calculator.calculate(searchBarInput);
				lastCalculatorResult = StonksUtils.DOUBLE_NUMBERS.format(result);
			} catch (Throwable ignored) {
				lastCalculatorResult = null;
			}
		}

		return lastCalculatorResult;
	}

	private void draw(@NotNull DrawContext context, @NotNull HandledScreen<?> handledScreen, Text displayText) {
		int x = REIRuntime.getInstance().getContextualSearchFieldLocation() == SearchFieldLocation.BOTTOM_SIDE
				? handledScreen.width / 2 + 90
				: handledScreen.width / 2 - 80;
		int y = handledScreen.height - 32;

		context.drawText(MinecraftClient.getInstance().textRenderer, displayText, x, y, Colors.WHITE.asInt(), false);
	}
}
