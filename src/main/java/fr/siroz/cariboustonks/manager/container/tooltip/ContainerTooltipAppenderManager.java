package fr.siroz.cariboustonks.manager.container.tooltip;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.crash.CrashType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.mixin.accessors.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Credits to the Skyblocker Team (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>)
 * for basic “how-to” logic in 1.20 with the Fabric API.
 */
public final class ContainerTooltipAppenderManager implements Manager {

	private final Map<Feature, ContainerTooltipAppender> tooltipAppenderMap = new LinkedHashMap<>();
	private final List<ContainerTooltipAppender> currentContainerTooltips = new ArrayList<>();

	@ApiStatus.Internal
	public ContainerTooltipAppenderManager() {
		ItemTooltipCallback.EVENT.register(this::onTooltipCallback);
		ScreenEvents.AFTER_INIT.register(this::onAfterInit);
	}

	@Override
	public void register(@NotNull Feature feature) {
		if (feature instanceof ContainerTooltipAppender containerTooltipAppender) {
			tooltipAppenderMap.put(feature, containerTooltipAppender);
		}
	}

	@EventHandler(event = "ItemTooltipCallback.EVENT")
	private void onTooltipCallback(
			ItemStack stack,
			Item.TooltipContext tooltipContext,
			TooltipType tooltipType,
			List<Text> lines
	) {
		if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> handledScreen) {
			appendToTooltip(((HandledScreenAccessor) handledScreen).getFocusedSlot(), stack, lines);
		} else {
			appendToTooltip(null, stack, lines);
		}
	}

	@EventHandler(event = "ScreenEvents.AFTER_INIT")
	private void onAfterInit(MinecraftClient minecraftClient, Screen screen, int scaledWidth, int scaledHeight) {
		currentContainerTooltips.clear();
		for (Map.Entry<Feature, ContainerTooltipAppender> appender : tooltipAppenderMap.entrySet()) {
			if (appender.getKey().isEnabled()) {
				if (appender.getKey() instanceof ContainerMatcherTrait trait && trait.matches(screen)) {
					currentContainerTooltips.add(appender.getValue());
				}
			}
		}

		currentContainerTooltips.sort(Comparator.comparingInt(ContainerTooltipAppender::getPriority));

		ScreenEvents.remove(screen).register(_screen -> currentContainerTooltips.clear());
	}

	private void appendToTooltip(Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (!SkyBlockAPI.isOnSkyBlock()) {
			return;
		}

		for (ContainerTooltipAppender appender : currentContainerTooltips) {
			try {
				appender.appendToTooltip(focusedSlot, stack, lines);
			} catch (Throwable throwable) {
				CaribouStonks.core().getCrashManager().reportCrash(CrashType.CONTAINER,
						appender.getClass().getSimpleName(),
						appender.getClass().getName(),
						"appendToTooltip", throwable
				);
			}
		}
	}
}
