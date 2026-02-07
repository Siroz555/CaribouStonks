package fr.siroz.cariboustonks.feature.garden;

import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.component.KeybindComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.input.KeyBind;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class MouseLockFeature extends Feature {

	private boolean locked = false;

	public MouseLockFeature() {
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((_mc, _level) -> this.onJoinWorld());
		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.namespaced("lockMouse", ctx -> {
					updateLockState();
					return 1;
				})
				.build());

		this.addComponent(KeybindComponent.class, KeybindComponent.builder()
				.add(new KeyBind("Garden Lock Mouse", GLFW.GLFW_KEY_MINUS, true, this::updateLockState))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && SkyBlockAPI.getIsland() == IslandType.GARDEN;
	}

	public boolean isLocked() {
		return locked;
	}

	@EventHandler(event = "ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE")
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
