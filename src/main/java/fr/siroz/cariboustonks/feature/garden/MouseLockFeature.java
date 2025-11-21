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
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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
					Component.literal("The mouse is no longer blocked due to a server change.").withStyle(ChatFormatting.RED));
		}
	}

	private void updateLockState() {
		if (!isEnabled()) return;

		locked = !locked;
		Component extra = Component.literal(" (/cariboustonks lockMouse)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
		Component message = locked ?
				Component.literal("The Mouse is locked").withStyle(ChatFormatting.RED).append(extra) :
				Component.literal("The Mouse is unlocked").withStyle(ChatFormatting.GREEN);
		Client.sendMessageWithPrefix(message);
	}
}
