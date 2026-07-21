package fr.siroz.cariboustonks.systems;

import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.system.System;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.jspecify.annotations.NonNull;

public final class CommandSystem implements System {

	public CommandSystem() {
	}

	@Override
	public void register(@NonNull Feature feature) {
		feature.getComponent(CommandComponent.class).ifPresent(this::registerComponent);
	}

	private void registerComponent(CommandComponent component) {
		for (CommandComponent.CommandRegistration registration : component.getRegistrations()) {
			ClientCommandRegistrationCallback.EVENT.register((d, _) -> registration.register(d));
		}
	}
}
