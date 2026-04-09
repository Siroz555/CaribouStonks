package fr.siroz.cariboustonks.features.hunting;

import fr.siroz.cariboustonks.core.component.KeybindComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.core.module.input.KeyBind;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.util.Client;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class QuickFusionFeature extends Feature {

	private static final Cooldown COOLDOWN = Cooldown.of(100, TimeUnit.MILLISECONDS);

	public QuickFusionFeature() {
		this.addComponent(KeybindComponent.class, KeybindComponent.builder()
				.add(new KeyBind("Quick Fusion - Repeat", GLFW.GLFW_KEY_UNKNOWN, this::onFusionKey))
				.add(new KeyBind("Quick Fusion - Confirm", GLFW.GLFW_KEY_UNKNOWN, this::onConfirmKey))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	private void onFusionKey(Screen screen, Slot slot) {
		handle(screen, "Fusion Box", 47);
	}

	private void onConfirmKey(Screen screen, Slot slot) {
		handle(screen, "Confirm Fusion", 33);
	}

	private void handle(Screen screen, String expectedTitle, int slotIndex) {
		if (!isEnabled()) return;
		if (!(screen instanceof AbstractContainerScreen<?> container)) return;
		if (!Objects.equals(container.getTitle().getString(), expectedTitle)) return;

		if (COOLDOWN.test()) {
			Client.handleMouseClick(container.getMenu().containerId, slotIndex, ContainerInput.PICKUP);
		}
	}
}
