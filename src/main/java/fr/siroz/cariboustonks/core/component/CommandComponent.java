package fr.siroz.cariboustonks.core.component;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.siroz.cariboustonks.system.CommandSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jspecify.annotations.NonNull;

/**
 * {@code Component} that provides client-side command registration for features.
 * <p>
 * Handled with <b>Fabric API Client Command v2</b>
 * <p>
 * This component simplifies the registration of client commands by abstracting
 * away the boilerplate code required by Brigadier. It supports both simple commands
 * with direct execution and complex commands with arguments and subcommands.
 *
 * <h2>Command Prefixes</h2>
 * Commands can be registered in two ways:
 * <ul>
 *   <li><strong>Namespaced</strong>: {@code /cariboustonks <command>} - grouped under the mod's namespace</li>
 *   <li><strong>Standalone</strong>: {@code /<command>} - registered as a top-level command</li>
 * </ul>
 *
 * <h2>Simple Example</h2>
 * <pre>{@code
 * addComponent(CommandComponent.class, CommandComponent.builder()
 *    // Registers: /cariboustonks lockMouse
 *    .namespaced("lockMouse", ctx -> {
 *        updateLockState();
 *        return 1;
 *    })
 *    // Registers: /cariboustonks debug
 *    .namespaced("debug", ctx -> {
 *        toggleDebug();
 *        return 1;
 *    })
 *    // Registers: /mycommand (standalone)
 *    .standalone("mycommand", ctx -> {
 *        doSomething();
 *        return 1;
 *     })
 *     .build()
 * );
 * }</pre>
 *
 * <h2>Advanced Example</h2>
 * <pre>{@code
 * addComponent(CommandComponent.class,
 *     CommandComponent.builder()
 *         // Complex command with arguments
 *         .namespaced("fetchPrice", builder -> builder
 *             .then(ClientCommandManager.argument("item", StringArgumentType.string())
 *                 .then(ClientCommandManager.argument("price", IntegerArgumentType.integer(0))
 *                     .executes(ctx -> {
 *                         // ...
 *                         return 1;
 *                     })
 *                 )
 *             )
 *         )
 *         .build()
 * );
 * }</pre>
 *
 * <h2>System Integration</h2>
 * This component is processed by {@link CommandSystem},
 * which registers all commands with the Fabric command dispatcher during client initialization.
 */
public final class CommandComponent implements Component {
	private final List<CommandRegistration> registrations;

	private CommandComponent(List<CommandRegistration> registrations) {
		this.registrations = List.copyOf(registrations);
	}

	public List<CommandRegistration> getRegistrations() {
		return registrations;
	}

	/**
	 * Creates a new builder for constructing {@link CommandComponent} instances.
	 *
	 * @return a new builder instance
	 */
	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a new builder with a custom namespace.
	 * <p>
	 * By default, the namespace is "cariboustonks".
	 *
	 * @param namespace the command namespace
	 * @return Builder
	 */
	@NonNull
	public static Builder builder(@NonNull String namespace) {
		return new Builder(namespace);
	}

	/**
	 * Functional interface for command execution.
	 *
	 * @see Command
	 */
	@FunctionalInterface
	public interface CommandExecutor {
		/**
		 * Executes the command.
		 *
		 * @param context the command context
		 * @return the command result (1 for success, 0 for failure)
		 */
		int execute(CommandContext<FabricClientCommandSource> context);
	}

	/**
	 * Functional interface for configuring complex commands.
	 *
	 * <p>This allows for advanced command building with arguments and subcommands
	 * while maintaining the fluent builder pattern.
	 */
	@FunctionalInterface
	public interface CommandConfigurator {
		/**
		 * Configures the command builder.
		 *
		 * @param builder the literal command builder to configure
		 */
		void configure(LiteralArgumentBuilder<FabricClientCommandSource> builder);
	}

	/**
	 * Internal interface for command registration.
	 */
	@FunctionalInterface
	public interface CommandRegistration {
		/**
		 * Registers the command with the dispatcher.
		 *
		 * @param dispatcher the command dispatcher
		 */
		void register(CommandDispatcher<FabricClientCommandSource> dispatcher);
	}

	/**
	 * Builder for constructing {@link CommandComponent} instances.
	 * <p>
	 * This builder provides a fluent API for registering both simple and complex
	 * commands. Commands can be namespaced under the mod's namespace or registered
	 * as standalone top-level commands.
	 *
	 * @see CommandComponent
	 */
	public static final class Builder {
		private final String namespace;
		private final List<CommandRegistration> registrations = new ArrayList<>();

		/**
		 * Creates a builder with the default namespace "cariboustonks".
		 */
		private Builder() {
			this("cariboustonks");
		}

		/**
		 * Creates a builder with a custom namespace.
		 *
		 * @param namespace the command namespace
		 */
		private Builder(@NonNull String namespace) {
			this.namespace = Objects.requireNonNull(namespace, "namespace must not be null");
		}

		/**
		 * Registers a simple namespaced command.
		 *
		 * <p>Creates a command in the format: {@code /<namespace> <name>}
		 * <br>Example: {@code /cariboustonks lockMouse}
		 *
		 * @param name     the command name
		 * @param executor the command executor
		 * @return Builder
		 */
		public Builder namespaced(@NonNull String name, @NonNull CommandExecutor executor) {
			Objects.requireNonNull(name, "command name must not be null");
			Objects.requireNonNull(executor, "executor must not be null");

			registrations.add(dispatcher -> {
				LiteralArgumentBuilder<FabricClientCommandSource> namespaceNode =
						ClientCommandManager.literal(namespace);

				LiteralArgumentBuilder<FabricClientCommandSource> commandNode =
						ClientCommandManager.literal(name).executes(executor::execute);

				dispatcher.register(namespaceNode.then(commandNode));
			});

			return this;
		}

		/**
		 * Registers a complex namespaced command with custom configuration.
		 *
		 * @param name         the command name
		 * @param configurator the command configurator
		 * @return Builder
		 */
		public Builder namespaced(@NonNull String name, @NonNull CommandConfigurator configurator) {
			Objects.requireNonNull(name, "command name must not be null");
			Objects.requireNonNull(configurator, "configurator must not be null");

			registrations.add(dispatcher -> {
				LiteralArgumentBuilder<FabricClientCommandSource> namespaceNode =
						ClientCommandManager.literal(namespace);

				LiteralArgumentBuilder<FabricClientCommandSource> commandNode =
						ClientCommandManager.literal(name);

				configurator.configure(commandNode);

				dispatcher.register(namespaceNode.then(commandNode));
			});

			return this;
		}

		/**
		 * Registers a simple standalone command.
		 * <p>
		 * Creates a top-level command in the format: {@code /<name>}
		 *
		 * @param name     the command name
		 * @param executor the command executor
		 * @return Builder
		 */
		@Deprecated // SIROZ-NOTE: marche pas
		public Builder standalone(@NonNull String name, @NonNull CommandExecutor executor) {
			Objects.requireNonNull(name, "command name must not be null");
			Objects.requireNonNull(executor, "executor must not be null");

			registrations.add(dispatcher -> dispatcher.register(
					ClientCommandManager.literal(name).executes(executor::execute)
			));

			return this;
		}

		/**
		 * Registers a complex standalone command with custom configuration.
		 *
		 * @param name         the command name
		 * @param configurator the command configurator
		 * @return Builder
		 */
		public Builder standalone(@NonNull String name, @NonNull CommandConfigurator configurator) {
			Objects.requireNonNull(name, "command name must not be null");
			Objects.requireNonNull(configurator, "configurator must not be null");

			registrations.add(dispatcher -> {
				LiteralArgumentBuilder<FabricClientCommandSource> commandNode =
						ClientCommandManager.literal(name);

				configurator.configure(commandNode);

				dispatcher.register(commandNode);
			});

			return this;
		}

		/**
		 * Registers a command with full manual control.
		 *
		 * <p>This method provides direct access to the dispatcher for cases where
		 * the simplified API is insufficient.
		 *
		 * @param registration the custom registration logic
		 * @return Builder
		 */
		public Builder custom(@NonNull CommandRegistration registration) {
			Objects.requireNonNull(registration, "registration must not be null");
			registrations.add(registration);
			return this;
		}

		/**
		 * Builds and returns a new {@link CommandComponent} instance.
		 *
		 * @return a new component instance
		 * @throws IllegalStateException if no commands have been registered
		 */
		@NonNull
		public CommandComponent build() {
			if (registrations.isEmpty()) {
				throw new IllegalStateException("At least one command must be registered");
			}
			return new CommandComponent(registrations);
		}
	}
}
