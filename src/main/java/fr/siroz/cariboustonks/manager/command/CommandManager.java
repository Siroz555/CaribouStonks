package fr.siroz.cariboustonks.manager.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.screen.CaribouStonksMenuScreen;
import fr.siroz.cariboustonks.screen.HudConfigScreen;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class CommandManager implements Manager {

	@ApiStatus.Internal
	public CommandManager() {
		ClientCommandRegistrationCallback.EVENT.register(this::registerModCommand);
	}

	@Override
	public void register(@NotNull Feature feature) {
		if (feature instanceof CommandRegistration commandRegistration) {
			ClientCommandRegistrationCallback.EVENT.register(commandRegistration::register);
		}
	}

	private void registerModCommand(
			@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher,
			CommandRegistryAccess registryAccess
	) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.executes(StonksUtils.openScreen(CaribouStonksMenuScreen::new))
				.then(ClientCommandManager.literal("config")
						.executes(StonksUtils.openScreen(() -> ConfigManager.createConfigGUI(null))))
				.then(ClientCommandManager.literal("hud")
						.executes(StonksUtils.openScreen(() -> HudConfigScreen.create(null))));

		LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(builder);
		dispatcher.register(ClientCommandManager.literal("stonks").redirect(node));
		dispatcher.register(ClientCommandManager.literal("cs").redirect(node));
	}
}
