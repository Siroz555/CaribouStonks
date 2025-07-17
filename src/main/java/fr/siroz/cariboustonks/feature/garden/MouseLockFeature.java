package fr.siroz.cariboustonks.feature.garden;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.manager.command.CommandRegistration;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindRegistration;
import fr.siroz.cariboustonks.util.Client;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MouseLockFeature extends Feature implements KeyBindRegistration, CommandRegistration {

	private boolean locked = false;

	public MouseLockFeature() {
		WorldEvents.JOIN.register(world -> onJoinWorld());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && SkyBlockAPI.getIsland() == IslandType.GARDEN;
	}

	@Override
	public @NotNull List<KeyBind> registerKeyBinds() {
		return List.of(new KeyBind("Lock Mouse", GLFW.GLFW_KEY_MINUS, true, this::updateLockState));
	}

	@Override
	public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("lockMouse").executes(context -> {
					updateLockState();
					return Command.SINGLE_SUCCESS;
				}))
		);
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
		if (!isEnabled()) {
			return;
		}

		if (checkPlayerAndWorldIsNull()) {
			return;
		}

		locked = !locked;
		Text extra = Text.literal(" (/cariboustonks lockMouse)").formatted(Formatting.GRAY, Formatting.ITALIC);
		Text message = locked ?
				Text.literal("The Mouse is locked").formatted(Formatting.RED).append(extra) :
				Text.literal("The Mouse is unlocked").formatted(Formatting.GREEN);
		Client.sendMessageWithPrefix(message);
	}
}
