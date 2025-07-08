package fr.siroz.cariboustonks.manager.command;

import com.mojang.brigadier.CommandDispatcher;
import fr.siroz.cariboustonks.feature.Feature;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to define a contract for registering custom
 * {@code client-side commands} within a {@link Feature}.
 * <p>
 * The purpose of this interface is to provide a structure for features that
 * require the definition of one or more commands. Each implementation should
 * supply the desired commands using the {@code register()} method,
 * which will be automatically registered via the {@link ClientCommandRegistrationCallback} Fabric API.
 * <h3>Exemples:</h3>
 * <pre>{@code
 * @Override
 * public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
 *     dispatcher.register(ClientCommandManager.literal("test")
 *                 .executes(context -> test(context.getSource())));
 * }
 * }</pre>
 * or
 * <pre>{@code
 * @Override
 * public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
 *     LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal("cmd)
 * 				.then(ClientCommandManager.literal("test1")
 * 						.executes(StonksUtils.openScreen(() -> ...)))
 * 				.then(ClientCommandManager.literal("test2")
 * 						.executes(this::test2));
 *
 * 		LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(builder);
 * 		dispatcher.register(ClientCommandManager.literal("a").redirect(node));
 * 		dispatcher.register(ClientCommandManager.literal("b").redirect(node));
 * }
 * }</pre>
 */
public interface CommandRegistration {

	/**
	 * Registers client-side commands with the provided command dispatcher provided by the {@code manager}
	 *
	 * @param dispatcher the {@link CommandDispatcher} used to register commands
	 */
	@ApiStatus.Internal
	void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess);
}
