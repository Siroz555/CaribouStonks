package fr.siroz.cariboustonks.features.hunting.tracking;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;

/**
 * Manages shard hunting session state and statistics computation.▶
 *
 * <pre>
 * State:
 *   IDLE ──[MIN_PRE_WARM catches]──▶ WARMING_UP ──[MIN_CATCHES reached]──▶ ACTIVE
 *     ▲                                  │                                    │
 *     └──── [inactivity reset] ──────────┘────────────────────────────────────┘
 * </pre>
 * NOTE : Je préfère mettre l'option au joueur sur le MIN_PRE_WARM au lieu d'avoir que UN seul catch.
 * Je n'ai pas mis en place ça au début, mais après des tests, en fishing ou en farming, on récupère
 * des shards de temps en temps sans forcément le vouloir, donc le HUD apparait.
 */
class ShardSession {
	public static final int MIN_CATCHES_FOR_STATS = 5;

	public enum State {
		IDLE,
		PRE_WARM,
		WARMING_UP,
		ACTIVE
	}

	private final List<ShardCatch> catches = new ArrayList<>();
	private final Map<String, Integer> shardTotals = new LinkedHashMap<>();
	private final Function<String, Double> coinResolver;
	private final Supplier<Long> inactivityResetMs;
	private final Supplier<Integer> minPreWarmCatch;

	// souvenir du concurrent au cas où
	private volatile State state = State.IDLE;
	private int preWarmCount = 0;
	private long sessionStart = 0L;
	private long lastCatchTime = 0L;
	private double totalCoins = 0.0;
	private int totalShards = 0;

	ShardSession(
			@NonNull Function<String, Double> coinResolver,
			@NonNull Supplier<Long> inactivityResetMs,
			@NonNull Supplier<Integer> minPreWarmCatch
	) {
		this.coinResolver = coinResolver;
		this.inactivityResetMs = inactivityResetMs;
		this.minPreWarmCatch = minPreWarmCatch;
	}

	/**
	 * Checks if the session {@link State} is {@code WARMING_UP} or {@code ACTIVE}.
	 *
	 * @return {@code true} if is running
	 */
	public boolean isRunning() {
		return state == ShardSession.State.WARMING_UP || state == ShardSession.State.ACTIVE;
	}

	/**
	 * Records un catch, update le state si besoin et retourne une nouvelle snapshot
	 * ou {@link Optional#empty()} si warming up ou en phase de pre-warm.
	 */
	public Optional<ShardSessionStats> recordCatch(@NonNull String shardType, int quantity) {
		long now = System.currentTimeMillis();

		// Inactivé
		if (state != State.IDLE && state != State.PRE_WARM && (now - lastCatchTime > inactivityResetMs.get())) {
			reset();
		}

		lastCatchTime = now;

		// Session boot
		if (state == State.IDLE || state == State.PRE_WARM) {
			state = State.PRE_WARM;
			preWarmCount++;
			if (preWarmCount < minPreWarmCatch.get()) {
				return Optional.empty();
			}
			// seuil atteint -> boot la session
			sessionStart = now;
			state = State.WARMING_UP;
			// Les catchs de PRE_WARM ne comptent pas dans les stats
		}

		// Accumulate
		catches.add(ShardCatch.now(shardType, quantity));
		shardTotals.merge(shardType, quantity, Integer::sum); // Permet de remapper avec/sans/null
		totalShards += quantity;
		totalCoins += (coinResolver.apply(shardType) * quantity);

		// State update
		if (state == State.WARMING_UP && catches.size() >= MIN_CATCHES_FOR_STATS) {
			state = State.ACTIVE;
		}

		return computeSnapshot(now);
	}

	public boolean tickInactivityCheck() {
		if (state == State.IDLE) return false;
		if (System.currentTimeMillis() - lastCatchTime > inactivityResetMs.get()) {
			reset();
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused") // Config option dans la feature et reset
	public void resetSession() {
		reset();
	}

	public State getState() {
		return state;
	}

	public int getCatchCount() {
		return catches.size();
	}

	public long getSessionStartMs() {
		return sessionStart;
	}

	public long getLastCatchTime() {
		return lastCatchTime;
	}

	private Optional<ShardSessionStats> computeSnapshot(long now) {
		if (state != State.ACTIVE) return Optional.empty();

		// Contrairement au Slayer Stats avec plusieurs stats et l'utilisation de SummaryStatistics,
		// ici, c'est un débit continu.
		// La méthode est total / durée, et non pas une moyenne d'échantillons.
		double hours = (now - sessionStart) / 3_600_000.0;
		return Optional.of(new ShardSessionStats(
				shardTotals,
				totalShards,
				totalCoins,
				hours > 0 ? totalShards / hours : 0.0,
				hours > 0 ? totalCoins / hours : 0.0,
				catches.size()
		));
	}

	private void reset() {
		catches.clear();
		shardTotals.clear();
		totalCoins = 0.0;
		totalShards = 0;
		sessionStart = 0L;
		lastCatchTime = 0L;
		preWarmCount = 0;
		state = State.IDLE;
	}

	record ShardCatch(@NonNull String shardType, int quantity, long timestamp) {
		public static ShardCatch now(@NonNull String shardType, int quantity) {
			return new ShardCatch(shardType, quantity, System.currentTimeMillis());
		}
	}
}
