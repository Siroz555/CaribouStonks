package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.mod.crash.CrashType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.system.System;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.mixin.accessors.AbstractContainerScreenAccessor;
import fr.siroz.cariboustonks.util.DeveloperTools;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class TooltipAppenderSystem implements System {

	private final Map<Feature, Pair<ContainerMatcherComponent, TooltipAppenderComponent>> registeredAppender = new LinkedHashMap<>();
	private final List<TooltipAppenderComponent> currentComponents = new ArrayList<>();

	@ApiStatus.Internal
	public TooltipAppenderSystem() {
		ItemTooltipCallback.EVENT.register(this::onTooltipCallback);
		ScreenEvents.AFTER_INIT.register(this::onAfterInit);
	}

	@Override
	public void register(@NotNull Feature feature) {
		Optional<ContainerMatcherComponent> matcherOpt = feature.getComponent(ContainerMatcherComponent.class);
		Optional<TooltipAppenderComponent> appenderOpt = feature.getComponent(TooltipAppenderComponent.class);

		if (matcherOpt.isEmpty() && appenderOpt.isPresent()) {
			CaribouStonks.LOGGER.warn("[TooltipAppenderSystem] no matcher found with appender for {}", feature.getShortName());
			return;
		}

		if (matcherOpt.isEmpty() || appenderOpt.isEmpty()) {
			return;
		}

		ContainerMatcherComponent matcher = matcherOpt.get();
		TooltipAppenderComponent appender = appenderOpt.get();

		registeredAppender.put(feature, Pair.of(matcher, appender));

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[TooltipAppenderSystem] Registered tooltip appender from feature: {}", feature.getShortName());
		}
	}

	@EventHandler(event = "ItemTooltipCallback.EVENT")
	private void onTooltipCallback(
            ItemStack stack,
            Item.TooltipContext tooltipContext,
            TooltipFlag _flags,
            List<Component> lines
	) {
		if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> containerScreen) {
			appendToTooltip(((AbstractContainerScreenAccessor) containerScreen).getFocusedSlot(), stack, lines);
		} else {
			appendToTooltip(null, stack, lines);
		}
	}

	@EventHandler(event = "ScreenEvents.AFTER_INIT")
	private void onAfterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		currentComponents.clear();
		for (Map.Entry<Feature, Pair<ContainerMatcherComponent, TooltipAppenderComponent>> appender : registeredAppender.entrySet()) {
			if (appender.getKey().isEnabled()) {
				if (appender.getValue().left().matches(screen, 0)) {
					currentComponents.add(appender.getValue().right());
				}
			}
		}

		currentComponents.sort(Comparator.comparingInt(TooltipAppenderComponent::getPriority));

		ScreenEvents.remove(screen).register(_screen -> currentComponents.clear());
	}

	private void appendToTooltip(Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (!SkyBlockAPI.isOnSkyBlock()) {
			return;
		}

		for (TooltipAppenderComponent component : currentComponents) {
			try {
				component.appendToTooltip(focusedSlot, stack, lines);
			} catch (Throwable throwable) {
				CaribouStonks.mod().getCrashManager().reportCrash(CrashType.CONTAINER,
						component.getClass().getSimpleName(),
						component.getClass().getName(),
						"appendToTooltip", throwable
				);
			}
		}
	}
}
