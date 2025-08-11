package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class RenderEvents {

	private RenderEvents() {
	}

	public static final Event<AllowRenderEntity> ALLOW_RENDER_ENTITY = EventFactory.createArrayBacked(AllowRenderEntity.class, listeners -> entity -> {
		for (AllowRenderEntity listener : listeners) {
			if (!listener.allowRenderEntity(entity)) {
				return false;
			}
		}
		return true;
	});

	@FunctionalInterface
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public interface AllowRenderEntity {
		boolean allowRenderEntity(@NotNull Entity entity);
	}
}
