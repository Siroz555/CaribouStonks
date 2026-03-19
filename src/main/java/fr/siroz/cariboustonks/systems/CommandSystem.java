package fr.siroz.cariboustonks.systems;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.system.System;
import fr.siroz.cariboustonks.screens.CaribouStonksMenuScreen;
import fr.siroz.cariboustonks.screens.HeldItemViewConfigScreen;
import fr.siroz.cariboustonks.screens.HudConfigScreen;
import fr.siroz.cariboustonks.util.Client;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.NonNull;

public final class CommandSystem implements System {

	public CommandSystem() {
		ClientCommandRegistrationCallback.EVENT.register(this::registerModCommand);
	}

	@Override
	public void register(@NonNull Feature feature) {
		feature.getComponent(CommandComponent.class).ifPresent(this::registerComponent);
	}

	private void registerComponent(CommandComponent component) {
		for (CommandComponent.CommandRegistration registration :  component.getRegistrations()) {
			ClientCommandRegistrationCallback.EVENT.register((d, _) ->  registration.register(d));
		}
	}

	private void registerModCommand(
            @NonNull CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess
	) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommands.literal(CaribouStonks.NAMESPACE)
				.executes(Client.openScreen(CaribouStonksMenuScreen::new))
				.then(ClientCommands.literal("config")
						.executes(Client.openScreen(() -> ConfigManager.createConfigGUI(null))))
				.then(ClientCommands.literal("hud")
						.executes(Client.openScreen(() -> HudConfigScreen.create(null))))
				.then(ClientCommands.literal("heldItemCustomization")
						.executes(context -> {
							context.getSource().sendError(Component.literal("Use /cariboustonks heldItemCustomization <mainHand/offHand>"));
							return 1;
						})
						.then(ClientCommands.literal("mainHand")
								.executes(Client.openScreen(() -> HeldItemViewConfigScreen.create(null, InteractionHand.MAIN_HAND))))
						.then(ClientCommands.literal("offHand")
								.executes(Client.openScreen(() -> HeldItemViewConfigScreen.create(null, InteractionHand.OFF_HAND)))));

		LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(builder);
		//dispatcher.register(ClientCommandManager.literal("caribou").redirect(node));
		dispatcher.register(ClientCommands.literal("cs").redirect(node));
	}
}
