package fr.siroz.cariboustonks.util.cooldown;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;

final class CooldownMapImpl<T> implements CooldownMap<T> {

	private final Cooldown base;
	private final LoadingCache<T, Cooldown> cache;

	CooldownMapImpl(@NonNull Cooldown base) {
		this.base = base;
		this.cache = CacheBuilder.newBuilder()
				.expireAfterAccess(base.getTimeout() + 10000L, TimeUnit.MILLISECONDS) // 10s after
				.build(new CacheLoader<>() {
					@Override
					public @NonNull Cooldown load(@NonNull T key) {
						return base.copy();
					}
				});
	}

	@NonNull
	@Override
	public Cooldown getBase() {
		return base;
	}

	@NonNull
	public Cooldown get(@NonNull T key) {
		return cache.getUnchecked(key);
	}

	@Override
	public void put(@NonNull T key, @NonNull Cooldown cooldown) {
		cache.put(key, cooldown);
	}

	@NonNull
	public Map<T, Cooldown> getAll() {
		return cache.asMap();
	}
}
