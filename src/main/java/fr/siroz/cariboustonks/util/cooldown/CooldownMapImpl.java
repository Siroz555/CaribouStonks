package fr.siroz.cariboustonks.util.cooldown;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

final class CooldownMapImpl<T> implements CooldownMap<T> {

	private final Cooldown base;
	private final LoadingCache<T, Cooldown> cache;

	CooldownMapImpl(@NotNull Cooldown base) {
		this.base = base;
		this.cache = CacheBuilder.newBuilder()
				.expireAfterAccess(base.getTimeout() + 10000L, TimeUnit.MILLISECONDS) // 10s after
				.build(new CacheLoader<>() {
					@Override
					public @NotNull Cooldown load(@NotNull T key) {
						return base.copy();
					}
				});
	}

	@NotNull
	@Override
	public Cooldown getBase() {
		return base;
	}

	@NotNull
	public Cooldown get(@NotNull T key) {
		return cache.getUnchecked(key);
	}

	@Override
	public void put(@NotNull T key, @NotNull Cooldown cooldown) {
		cache.put(key, cooldown);
	}

	@NotNull
	public Map<T, Cooldown> getAll() {
		return cache.asMap();
	}
}
