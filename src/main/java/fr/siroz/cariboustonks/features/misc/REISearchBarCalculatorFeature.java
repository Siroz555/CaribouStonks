package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.mod.integration.RoughlyEnoughItemsIntegration;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.Calculator;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class REISearchBarCalculatorFeature extends Feature {

	private String lastSearchBarInput = "";
	@Nullable
	private String lastCalculatorResult = null;

	public REISearchBarCalculatorFeature() {
		ScreenEvents.BEFORE_INIT.register(this::onScreenBeforeInit);
	}

	@Override
	public boolean isEnabled() {
		return this.config().misc.compatibility.reiSearchBarCalculator
				&& RoughlyEnoughItemsIntegration.isModLoaded();
	}

	@EventHandler(event = "ScreenEvents.BEFORE_INIT")
	private void onScreenBeforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (isEnabled() && screen instanceof AbstractContainerScreen<?> handledScreen) {
			ScreenEvents.afterRender(screen).register((_screen, context, mouseX, mouseY, tickDelta) -> {
				String searchBarText = RoughlyEnoughItemsIntegration.getSearchBarText();
				if (searchBarText == null || searchBarText.isBlank() || !searchBarText.matches(".*\\d.*")) {
					return;
				}

				Component calculatorTextResult = getCalculatorTextResult(searchBarText);
				if (calculatorTextResult.getString().contains("=")) {
					draw(context, handledScreen, calculatorTextResult);
				}
			});
		}
	}

	private @NonNull Component getCalculatorTextResult(String searchBarFieldText) {
		String result = calculate(searchBarFieldText);
		return result == null ? Component.empty() : Component.literal(searchBarFieldText).withStyle(ChatFormatting.GREEN)
				.append(Component.literal(" = ").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(result).withStyle(ChatFormatting.GREEN));
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

	private void draw(@NonNull GuiGraphics context, @NonNull AbstractContainerScreen<?> handledScreen, Component displayText) {
		int x = RoughlyEnoughItemsIntegration.isSearchBarAtBottomSide() ? handledScreen.width / 2 + 90 : handledScreen.width / 2 - 80;
		int y = handledScreen.height - 32;

		context.drawString(Minecraft.getInstance().font, displayText, x, y, Colors.WHITE.asInt(), false);
	}
}
