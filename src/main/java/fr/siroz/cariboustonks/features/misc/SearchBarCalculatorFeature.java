package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.mod.integration.JustEnoughItemsIntegration;
import fr.siroz.cariboustonks.core.mod.integration.RoughlyEnoughItemsIntegration;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.Calculator;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SearchBarCalculatorFeature extends Feature {

	private String lastSearchBarInput = "";
	@Nullable
	private String lastCalculatorResult = null;

	public SearchBarCalculatorFeature() {
		ScreenEvents.BEFORE_INIT.register(this::onScreenBeforeInit);
	}

	@Override
	public boolean isEnabled() {
		return this.config().misc.compatibility.reiSearchBarCalculator
				&& (RoughlyEnoughItemsIntegration.isModLoaded() || JustEnoughItemsIntegration.isModLoaded());
	}

	@EventHandler(event = "ScreenEvents.BEFORE_INIT")
	private void onScreenBeforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (isEnabled() && screen instanceof AbstractContainerScreen<?> handledScreen) {
			ScreenEvents.afterExtract(screen).register((_, context, _, _, _) -> {

				ModIntegration integration = resolveActiveIntegration();
				if (integration == null) return;

				String searchInput = integration.getSearchBarText();
				if (!isValidCalculatorInput(searchInput)) return;

				Component result = buildCalculatorComponent(searchInput);
				if (!result.getString().contains("=")) return;

				this.drawResult(context, handledScreen, result, integration);
			});
		}
	}

	@Nullable
	private ModIntegration resolveActiveIntegration() {
		if (RoughlyEnoughItemsIntegration.isModLoaded()) return ModIntegration.REI;
		if (JustEnoughItemsIntegration.isModLoaded()) return ModIntegration.JEI;
		return null;
	}

	private boolean isValidCalculatorInput(@Nullable String input) {
		return input != null && !input.isBlank() && input.matches(".*\\d.*");
	}

	private @NonNull Component buildCalculatorComponent(String searchInput) {
		String result = evaluateExpression(searchInput);
		if (result == null) return Component.empty();

		return Component.literal(searchInput).withStyle(ChatFormatting.GREEN)
				.append(Component.literal(" = ").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(result).withStyle(ChatFormatting.GREEN));
	}

	@Nullable
	private String evaluateExpression(String searchInput) {
		if (!lastSearchBarInput.equals(searchInput)) {
			lastSearchBarInput = searchInput;
			try {
				double result = Calculator.calculate(searchInput);
				lastCalculatorResult = StonksUtils.DOUBLE_NUMBERS.format(result);
			} catch (Throwable _) {
				lastCalculatorResult = null;
			}
		}
		return lastCalculatorResult;
	}

	private void drawResult(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, Component text, ModIntegration integration) {
		int textWidth = ClientContext.getFont().width(text);

		graphics.text(ClientContext.getFont(), text,
				integration.getTextX(screen, textWidth),
				integration.getTextY(screen),
				Colors.WHITE.asInt(), false
		);
	}

	private enum ModIntegration {
		REI {
			@Override
			public String getSearchBarText() {
				return RoughlyEnoughItemsIntegration.getSearchBarText();
			}

			@Override
			public int getTextX(AbstractContainerScreen<?> screen, int textWidth) {
				return RoughlyEnoughItemsIntegration.isSearchBarAtBottomSide()
						? screen.width / 2 + 90
						: screen.width / 2 - 80;
			}

			@Override
			public int getTextY(AbstractContainerScreen<?> screen) {
				return screen.height - 32;
			}
		},
		JEI {
			@Override
			public String getSearchBarText() {
				return JustEnoughItemsIntegration.getSearchBarText();
			}

			@Override
			public int getTextX(AbstractContainerScreen<?> screen, int textWidth) {
				return JustEnoughItemsIntegration.isSearchBarAtCenter()
						? screen.width / 2 - 90
						: screen.width - textWidth - 12; // 12 = marge a droite
			}

			@Override
			public int getTextY(AbstractContainerScreen<?> screen) {
				return screen.height - 36;
			}
		};

		public abstract String getSearchBarText();

		public abstract int getTextX(AbstractContainerScreen<?> screen, int textWidth);

		public abstract int getTextY(AbstractContainerScreen<?> screen);
	}
}
