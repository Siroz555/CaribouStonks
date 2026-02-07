package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.mod.crash.CrashType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.system.System;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.mixin.accessors.AbstractContainerScreenAccessor;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class ContainerOverlaySystem implements System {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final Map<Feature, Pair<ContainerMatcherComponent, ContainerOverlayComponent>> registeredOverlays = new HashMap<>();
	private ContainerOverlayComponent currentContainerOverlay = null;
	private List<ColorHighlight> highlights;

	public ContainerOverlaySystem() {
		ScreenEvents.BEFORE_INIT.register(this::onScreenBeforeInit);
	}

	@Override
	public void register(@NonNull Feature feature) {
		Optional<ContainerMatcherComponent> matcherOpt = feature.getComponent(ContainerMatcherComponent.class);
		Optional<ContainerOverlayComponent> overlayOpt = feature.getComponent(ContainerOverlayComponent.class);

		if (matcherOpt.isEmpty() && overlayOpt.isPresent()) {
			CaribouStonks.LOGGER.error("[ContainerOverlaySystem] no matcher found with overlay for {}", feature.getShortName());
			return;
		}

		if (matcherOpt.isEmpty() || overlayOpt.isEmpty()) {
			return;
		}

		ContainerMatcherComponent matcher = matcherOpt.get();
		ContainerOverlayComponent overlay = overlayOpt.get();

		registeredOverlays.put(feature, Pair.of(matcher, overlay));

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[ContainerOverlaySystem] Registered overlay from feature: {}", feature.getShortName());
		}
	}

	@EventHandler(event = "ScreenEvents.BEFORE_INIT")
	private void onScreenBeforeInit(Minecraft _client, Screen screen, int _scaledWidth, int _scaledHeight) {
		if (SkyBlockAPI.isOnSkyBlock() && screen instanceof ContainerScreen containerScreen) {
			ScreenEvents.remove(screen).register(_screen -> clearScreen());
			onScreen(containerScreen);
		} else {
			clearScreen();
		}
	}

	public void markHighlightsDirty() {
		highlights = null;
	}

	public void draw(GuiGraphics context, AbstractContainerScreen<ChestMenu> containerScreen, List<Slot> slots) {
		if (currentContainerOverlay == null) {
			return;
		}

		int screenWidth = CLIENT.getWindow().getGuiScaledWidth();
		int screenHeight = CLIENT.getWindow().getGuiScaledHeight();
		try {
			currentContainerOverlay.render(context, screenWidth, screenHeight, 0, 0);
		} catch (Throwable throwable) {
			CaribouStonks.mod().getCrashManager().reportCrash(CrashType.CONTAINER,
					currentContainerOverlay.getClass().getSimpleName(),
					currentContainerOverlay.getClass().getName(),
					"render", throwable
			);
		}

		context.pose().pushMatrix();
		context.pose().translate(
				((AbstractContainerScreenAccessor) containerScreen).getX(),
				((AbstractContainerScreenAccessor) containerScreen).getY()
		);

		if (highlights == null) {
			highlights = currentContainerOverlay.analyzeContent(
					slotMap(slots.subList(0, containerScreen.getMenu().getRowCount() * 9))
			);
		}

		// NPE - Cannot invoke "java.util.List.iterator()" because "this.highlights" is null.
		// J'ai crash qu'une seule fois, je ne sais pas pourquoi donc tout bêtement un try-catch.
		// Je sais que la boucle for fait un iterator à chaque fois, est-ce que c'est le délai ?
		try {
			for (ColorHighlight highlight : highlights) {
				Slot slot = slots.get(highlight.slot());
				context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, highlight.color().asInt());
			}
		} catch (Throwable ignored) {
		}

		context.pose().popMatrix();
	}

	private void onScreen(@NonNull ContainerScreen screen) {
		for (Map.Entry<Feature, Pair<ContainerMatcherComponent, ContainerOverlayComponent>> overlay : registeredOverlays.entrySet()) {
			if (overlay.getKey().isEnabled()) {
				if (overlay.getValue().left().matches(screen, screen.getMenu().slots.size())) {
					currentContainerOverlay = overlay.getValue().right();
					markHighlightsDirty();
					return;
				}
			}
		}

		clearScreen();
	}

	private void clearScreen() {
		if (currentContainerOverlay != null) {
			currentContainerOverlay.reset();
			currentContainerOverlay = null;
		}
	}

	@NonNull
	private Int2ObjectMap<ItemStack> slotMap(@NonNull List<Slot> slots) {
		Int2ObjectMap<ItemStack> slotMap = new Int2ObjectRBTreeMap<>();
		for (int i = 0; i < slots.size(); i++) {
			slotMap.put(i, slots.get(i).getItem());
		}

		return slotMap;
	}
}
