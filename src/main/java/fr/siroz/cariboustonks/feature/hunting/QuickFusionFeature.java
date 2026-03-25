package fr.siroz.cariboustonks.feature.hunting;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindComponent;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class QuickFusionFeature extends Feature {

	private static final Cooldown COOLDOWN = Cooldown.of(100, TimeUnit.MILLISECONDS);

	public QuickFusionFeature() {
		this.addComponent(KeyBindComponent.class, () -> List.of(
				new KeyBind("Quick Fusion - Repeat", GLFW.GLFW_KEY_UNKNOWN, this::onFusionKey),
				new KeyBind("Quick Fusion - Confirm", GLFW.GLFW_KEY_UNKNOWN, this::onConfirmKey)
		));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && SkyBlockAPI.getIsland() == IslandType.GALATEA;
	}

	private void onFusionKey(Screen screen, Slot slot) {
		handle(screen, "Fusion Box", 47);
	}

	private void onConfirmKey(Screen screen, Slot slot) {
		handle(screen, "Confirm Fusion", 33);
	}

	private void handle(Screen screen, String expectedTitle, int slotIndex) {
		if (!isEnabled()) return;
		if (!(screen instanceof HandledScreen<?> container)) return;
		if (!Objects.equals(container.getTitle().getString(), expectedTitle)) return;

		if (COOLDOWN.test()) {
			Client.handleMouseClick(container.getScreenHandler().syncId, slotIndex, SlotActionType.PICKUP);
		}
	}
}
