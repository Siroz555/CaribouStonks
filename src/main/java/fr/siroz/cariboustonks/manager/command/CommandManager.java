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
import fr.siroz.cariboustonks.screen.HeldItemViewConfigScreen;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class CommandManager implements Manager {

	@ApiStatus.Internal
	public CommandManager() {
		ClientCommandRegistrationCallback.EVENT.register(this::registerModCommand);
	}

	@Override
	public void register(@NotNull Feature feature) {
		feature.getComponent(CommandComponent.class)
				.ifPresent(cc -> ClientCommandRegistrationCallback.EVENT.register((d, _ra) -> cc.register(d)));
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
						.executes(StonksUtils.openScreen(() -> HudConfigScreen.create(null))))
				.then(ClientCommandManager.literal("heldItemCustomization")
						.executes(context -> {
							context.getSource().sendError(Text.literal("Use /cariboustonks heldItemView <mainHand/offHand>"));
							return 1;
						})
						.then(ClientCommandManager.literal("mainHand")
								.executes(StonksUtils.openScreen(() -> HeldItemViewConfigScreen.create(null, Hand.MAIN_HAND))))
						.then(ClientCommandManager.literal("offHand")
								.executes(StonksUtils.openScreen(() -> HeldItemViewConfigScreen.create(null, Hand.OFF_HAND)))));

		LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(builder);
		//dispatcher.register(ClientCommandManager.literal("caribou").redirect(node));
		dispatcher.register(ClientCommandManager.literal("cs").redirect(node));
	}
}
