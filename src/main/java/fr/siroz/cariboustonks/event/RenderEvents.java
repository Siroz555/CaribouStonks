package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class RenderEvents {

	private RenderEvents() {
	}

	public static final Event<RenderEntity> RENDER_ENTITY_CANCELLABLE = EventFactory.createArrayBacked(RenderEntity.class, listeners -> entity -> {
		for (RenderEntity listener : listeners) {
			if (listener.onRenderEntity(entity)) {
				return true;
			}
		}
		return false;
	});

	@FunctionalInterface
	public interface RenderEntity {
		boolean onRenderEntity(@NotNull Entity entity);
	}
}
