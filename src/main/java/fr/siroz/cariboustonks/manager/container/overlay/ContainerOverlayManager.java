package fr.siroz.cariboustonks.manager.container.overlay;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.crash.CrashType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.mixin.accessors.HandledScreenAccessor;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
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

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

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
	private void onScreenBeforeInit(MinecraftClient _client, Screen screen, int _scaledWidth, int _scaledHeight) {
		if (SkyBlockAPI.isOnSkyBlock() && screen instanceof GenericContainerScreen genericContainerScreen) {
			ScreenEvents.remove(screen).register(_screen -> clearScreen());
			onScreen(genericContainerScreen);
		} else {
			clearScreen();
		}
	}

	public void markHighlightsDirty() {
		highlights = null;
	}

	public void draw(DrawContext context, HandledScreen<GenericContainerScreenHandler> handledScreen, List<Slot> slots) {
		if (currentContainerOverlay == null) {
			return;
		}

		int screenWidth = CLIENT.getWindow().getScaledWidth();
		int screenHeight = CLIENT.getWindow().getScaledHeight();
		try {
			currentContainerOverlay.render(context, screenWidth, screenHeight, 0, 0);
		} catch (Throwable throwable) {
			CaribouStonks.core().getCrashManager().reportCrash(CrashType.CONTAINER,
					currentContainerOverlay.getClass().getSimpleName(),
					currentContainerOverlay.getClass().getName(),
					"render", throwable
			);
		}

		context.getMatrices().pushMatrix();
		context.getMatrices().translate(((HandledScreenAccessor) handledScreen).getX(), ((HandledScreenAccessor) handledScreen).getY());

		if (highlights == null) {
			highlights = currentContainerOverlay.content(
					slotMap(slots.subList(0, handledScreen.getScreenHandler().getRows() * 9))
			);
		}

		for (ColorHighlight highlight : highlights) {
			Slot slot = slots.get(highlight.slot());
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, highlight.color().asInt());
		}

		context.getMatrices().popMatrix();
	}

	private void onScreen(@NotNull GenericContainerScreen screen) {
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
			slotMap.put(i, slots.get(i).getStack());
		}

		return slotMap;
	}
}
