package fr.siroz.cariboustonks.event;

import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;

/**
 * Events related to world rendering.
 */
public final class RenderEvents {

	private RenderEvents() {
	}

	/**
	 * Called after entity, terrain and particle translucent layers have been drawn (AFTER_TRANSLUCENT).
	 * <p>
	 * {@link WorldRenderer} is provided as parameter.
	 */
	public static final Event<WorldRender> WORLD_RENDER_EVENT = EventFactory.createArrayBacked(WorldRender.class, listeners -> renderer -> {
		for (WorldRender listener : listeners) {
			listener.onWorldRender(renderer);
		}
	});

	/**
	 * Called before an entity is rendered.
	 */
	public static final Event<AllowRenderEntity> ALLOW_RENDER_ENTITY_EVENT = EventFactory.createArrayBacked(AllowRenderEntity.class, listeners -> entity -> {
		for (AllowRenderEntity listener : listeners) {
			if (!listener.allowRenderEntity(entity)) {
				return false;
			}
		}
		return true;
	});

	@FunctionalInterface
	public interface WorldRender {
		void onWorldRender(WorldRenderer renderer);
	}

	@FunctionalInterface
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public interface AllowRenderEntity {
		boolean allowRenderEntity(@NonNull Entity entity);
	}
}
