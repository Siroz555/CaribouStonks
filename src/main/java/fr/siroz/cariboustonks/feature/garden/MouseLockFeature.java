package fr.siroz.cariboustonks.feature.garden;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindComponent;
import fr.siroz.cariboustonks.util.Client;
import java.util.Collections;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public final class MouseLockFeature extends Feature {

	private boolean locked = false;

	public MouseLockFeature() {
		WorldEvents.JOIN.register(world -> onJoinWorld());

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("lockMouse").executes(context -> {
					updateLockState();
					return 1;
				}))
		));

		addComponent(KeyBindComponent.class, () -> Collections.singletonList(
				new KeyBind("Garden Lock Mouse", GLFW.GLFW_KEY_MINUS, true, this::updateLockState)
		));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && SkyBlockAPI.getIsland() == IslandType.GARDEN;
	}

	public boolean isLocked() {
		return locked;
	}

	@EventHandler(event = "WorldEvents.JOIN")
	private void onJoinWorld() {
		if (locked) {
			locked = false;
			Client.sendMessageWithPrefix(
					Text.literal("The mouse is no longer blocked due to a server change.").formatted(Formatting.RED));
		}
	}

	private void updateLockState() {
		if (!isEnabled()) return;

		locked = !locked;
		Text extra = Text.literal(" (/cariboustonks lockMouse)").formatted(Formatting.GRAY, Formatting.ITALIC);
		Text message = locked ?
				Text.literal("The Mouse is locked").formatted(Formatting.RED).append(extra) :
				Text.literal("The Mouse is unlocked").formatted(Formatting.GREEN);
		Client.sendMessageWithPrefix(message);
	}
}
