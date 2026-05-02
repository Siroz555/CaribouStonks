package fr.siroz.cariboustonks.platform.impl;

import fr.siroz.cariboustonks.platform.api.WorldContext;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Vanilla-backed implementation of {@link WorldContext}.
 */
public final class VanillaWorldContext implements WorldContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Override
	public boolean isAvailable() {
		return CLIENT.level != null;
	}

	@Override
	public long getWorldDay() {
		return isAvailable() ? CLIENT.level.getOverworldClockTime() / 24000 : 0L;
	}

	@Override
	public long getWorldTime() {
		return isAvailable() ? CLIENT.level.getGameTime() : 0;
	}

	@Override
	public @NonNull Iterable<Entity> getEntities() {
		return isAvailable() ? CLIENT.level.entitiesForRendering() : Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> findClosestEntities(@NonNull Class<T> entity, double distanceInBlocks, @NonNull Predicate<? super T> entityPredicate) {
		if (CLIENT.player == null || !isAvailable()) return Collections.emptyList();

		return CLIENT.level.getEntitiesOfClass(entity, CLIENT.player.getBoundingBox().inflate(distanceInBlocks), entityPredicate);
	}

	@Override
	public @Nullable <T extends Entity> T findClosestEntity(@NonNull Class<T> entity, double distanceInBlocks, @NonNull Predicate<? super T> entityPredicate, @NonNull Predicate<? super T> filterPredicate) {
		if (CLIENT.player == null || !isAvailable()) return null;

		List<T> entities = findClosestEntities(entity, distanceInBlocks, entityPredicate);
		return findClosestEntity(entities, filterPredicate);
	}

	@Override
	public @Nullable <T extends Entity> T findClosestEntity(@NonNull List<T> entities, @NonNull Predicate<? super T> filterPredicate) {
		if (CLIENT.player == null || !isAvailable()) return null;
		if (entities.isEmpty()) return null;

		return entities.stream()
				.filter(filterPredicate)
				.min(Comparator.comparingDouble(as -> as.distanceToSqr(CLIENT.player)))
				.orElse(null);
	}
}
