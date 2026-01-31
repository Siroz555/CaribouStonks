package fr.siroz.cariboustonks.system.command;

import com.mojang.brigadier.CommandDispatcher;
import fr.siroz.cariboustonks.system.Component;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

/**
 * Capability component for registering client-side commands for a {@code Feature}.
 * <p>
 * A {@code CommandComponent} is discovered by the {@link CommandSystem}
 * and wired into Fabric’s {@link ClientCommandRegistrationCallback}.
 * <p>
 * Registration is one-shot and happens at the client init time.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * addComponent(CommandComponent.class, dispatcher -> {
 *     dispatcher.register(ClientCommandManager.literal("test")
 *     		.executes(ctx -> 1));
 * });
 * }</pre>
 */
public interface CommandComponent extends Component {

	/**
	 * Registers client-side commands with the provided command dispatcher provided by the {@code manager}
	 *
	 * @param dispatcher the {@link CommandDispatcher} used to register commands
	 */
	void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher);
}
