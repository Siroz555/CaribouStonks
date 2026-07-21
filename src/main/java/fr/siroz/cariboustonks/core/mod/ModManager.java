package fr.siroz.cariboustonks.core.mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.mod.changelog.ChangelogManager;
import fr.siroz.cariboustonks.core.mod.crash.CrashManager;
import fr.siroz.cariboustonks.core.mod.dev.DeveloperManager;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.screens.CaribouStonksMenuScreen;
import fr.siroz.cariboustonks.screens.HeldItemViewConfigScreen;
import fr.siroz.cariboustonks.screens.HudConfigScreen;
import fr.siroz.cariboustonks.util.DeveloperTools;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.NonNull;

/**
 * The {@code ModManager} class serves as the core manager for the mod.
 */
public final class ModManager {

	private final CrashManager crashManager;
	private final ModDataSource modDataSource;
	private final SecretModFeatures secretModFeatures;

	public ModManager() {
		this.crashManager = new CrashManager();
		this.modDataSource = new ModDataSource();
		this.secretModFeatures = new SecretModFeatures();

		new UpdateChecker();
		new ChangelogManager();
		new WelcomeMessage();

		// Developer Mode
		if (DeveloperTools.isInDevelopment()) {
			new DeveloperManager();
		}

		// Commands
		ClientCommandRegistrationCallback.EVENT.register(this::registerModCommand);
	}

	/**
	 * Retrieves the {@link CrashManager} instance.
	 *
	 * @return the {@link CrashManager} instance.
	 */
	public @NonNull CrashManager getCrashManager() {
		return crashManager;
	}

	/**
	 * Retrieves the {@link ModDataSource} instance.
	 *
	 * @return the {@link ModDataSource} instance
	 */
	public @NonNull ModDataSource getModDataSource() {
		return modDataSource;
	}

	/**
	 * Retrieves the {@link SecretModFeatures} instance.
	 *
	 * @return the {@link SecretModFeatures} instance
	 */
	public SecretModFeatures getSecretModFeatures() {
		return secretModFeatures;
	}

	private void registerModCommand(@NonNull CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext ra) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommands.literal(CaribouStonks.NAMESPACE)
				.executes(ClientContext.openScreen(CaribouStonksMenuScreen::new))
				.then(ClientCommands.literal("config")
						.executes(ClientContext.openScreen(() -> ConfigManager.createConfigGUI(null))))
				.then(ClientCommands.literal("hud")
						.executes(ClientContext.openScreen(() -> HudConfigScreen.create(null))))
				.then(ClientCommands.literal("heldItemCustomization")
						.executes(context -> {
							context.getSource().sendError(Component.literal("Use /cariboustonks heldItemCustomization <mainHand/offHand>"));
							return 1;
						})
						.then(ClientCommands.literal("mainHand")
								.executes(ClientContext.openScreen(() -> HeldItemViewConfigScreen.create(null, InteractionHand.MAIN_HAND))))
						.then(ClientCommands.literal("offHand")
								.executes(ClientContext.openScreen(() -> HeldItemViewConfigScreen.create(null, InteractionHand.OFF_HAND)))))
				.then(ClientCommands.literal("reload")
						.executes(context -> {
							context.getSource().sendError(Component.literal("Use /cariboustonks reload <items/attributes>"));
							return 1;
						})
						.then(ClientCommands.literal("items").executes(_ -> {
							CaribouStonks.skyBlock().getHypixelDataSource().reload();
							return 1;
						}))
						.then(ClientCommands.literal("attributes").executes(_ -> {
							modDataSource.reload();
							return 1;
						})));

		LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(builder);
		//dispatcher.register(ClientCommandManager.literal("caribou").redirect(node));
		dispatcher.register(ClientCommands.literal("cs").redirect(node));
	}
}
