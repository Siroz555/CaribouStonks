package fr.siroz.cariboustonks.manager.container.overlay;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.crash.CrashType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.mixin.accessors.AbstractContainerScreenAccessor;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Credits to the Skyblocker Team (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>)
 * for basic “how-to” logic in 1.20 with the Fabric API.
 */
public final class ContainerOverlayManager implements Manager {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final Map<Feature, ContainerOverlay> containerOverlayMap = new HashMap<>();
	private ContainerOverlay currentContainerOverlay = null;
	private List<ColorHighlight> highlights;

	@ApiStatus.Internal
	public ContainerOverlayManager() {
		ScreenEvents.BEFORE_INIT.register(this::onScreenBeforeInit);
	}

	@Override
	public void register(@NotNull Feature feature) {
		if (feature instanceof ContainerOverlay containerOverlay) {
			containerOverlayMap.put(feature, containerOverlay);
		}
	}

	@EventHandler(event = "ScreenEvents.BEFORE_INIT")
	private void onScreenBeforeInit(Minecraft _client, Screen screen, int _scaledWidth, int _scaledHeight) {
		if (SkyBlockAPI.isOnSkyBlock() && screen instanceof ContainerScreen genericContainerScreen) {
			ScreenEvents.remove(screen).register(_screen -> clearScreen());
			onScreen(genericContainerScreen);
		} else {
			clearScreen();
		}
	}

	public void markHighlightsDirty() {
		highlights = null;
	}

	public void draw(GuiGraphics context, AbstractContainerScreen<@NotNull ChestMenu> handledScreen, List<Slot> slots) {
		if (currentContainerOverlay == null) {
			return;
		}

		int screenWidth = CLIENT.getWindow().getGuiScaledWidth();
		int screenHeight = CLIENT.getWindow().getGuiScaledHeight();
		try {
			currentContainerOverlay.render(context, screenWidth, screenHeight, 0, 0);
		} catch (Throwable throwable) {
			CaribouStonks.core().getCrashManager().reportCrash(CrashType.CONTAINER,
					currentContainerOverlay.getClass().getSimpleName(),
					currentContainerOverlay.getClass().getName(),
					"render", throwable
			);
		}

		context.pose().pushMatrix();
		context.pose().translate(((AbstractContainerScreenAccessor) handledScreen).getX(), ((AbstractContainerScreenAccessor) handledScreen).getY());

		if (highlights == null) {
			highlights = currentContainerOverlay.content(
					slotMap(slots.subList(0, handledScreen.getMenu().getRowCount() * 9))
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

	private void onScreen(@NotNull ContainerScreen screen) {
		for (Map.Entry<Feature, ContainerOverlay> overlay : containerOverlayMap.entrySet()) {
			if (overlay.getKey().isEnabled()) {
				if (overlay.getKey() instanceof ContainerMatcherTrait trait && trait.matches(screen)) {
					currentContainerOverlay = overlay.getValue();
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

	@NotNull
	private Int2ObjectMap<ItemStack> slotMap(@NotNull List<Slot> slots) {
		Int2ObjectMap<ItemStack> slotMap = new Int2ObjectRBTreeMap<>();
		for (int i = 0; i < slots.size(); i++) {
			slotMap.put(i, slots.get(i).getItem());
		}

		return slotMap;
	}
}
