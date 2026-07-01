package fr.siroz.cariboustonks.features.misc.bestiary;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * État de suivi pour une entré& du Bestiary.
 * <p>
 * Maintient une fenêtre "glissante" (5mins) des derniers kills détectés afin de calculer
 * un taux de kills/heure réactif, ainsi qu'une estimation du temps restant avant le prochain tier.
 * <p>
 * Le système est volontairement agnostique de la cadence de polling : le TabList
 * ne se met à jour côté serveur que toutes les ~3 secondes alors que {@code update()}
 * peut être appelé a ~ une seconde. Un appel sans progression (delta = 0) est un no-op pur,
 * donc le comportement reste identique quelle que soit la fréquence d'appel.
 * Seul {@link #lastProgressTime} (dernier kill réel, distinct du dernier poll) sert
 * de référence pour la détection d'inactivité et le calcul du taux.
 */
final class BestiaryMobStats {
	private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(2);
	private static final Duration RATE_WINDOW = Duration.ofMinutes(5);
	private static final long MIN_KILLS = 10; // 5 // trop faible, mais en fishing c'est horrible, enfin...
	private static final long MIN_KILLS_FOR_STATS = 15; // 20 // 5 de +/- est largement mieux coté visibilité dans le HUD

	private final Deque<KillEvent> recentKills = new ArrayDeque<>();

	private int tier;
	private long current;
	private long max;
	private Instant lastProgressTime;
	private Component lineComponent;

	private record KillEvent(Instant timestamp, long amount) {
	}

	BestiaryMobStats(@NonNull BestiaryEntry entry, @NonNull Instant now) {
		this.tier = entry.tier();
		this.current = entry.current();
		this.max = entry.max();
		this.lastProgressTime = now;
		this.lineComponent = entry.lineComponent();
	}

	public void update(@NonNull BestiaryEntry entry, @NonNull Instant now) {
		lineComponent = entry.lineComponent();
		boolean tierChanged = entry.tier() != tier;

		// Un appel sans progression (delta = 0) est un no-op : ni lastProgressTime ni la fenêtre de kills
		// ne sont touchés, seul le Component est rafraîchi.

		if (tierChanged) {
			// Pas de delta ici dans le cas ou le serveur, nous envoi ke passage au palier suivant.
			tier = entry.tier();
			max = entry.max();
			current = entry.current();
			lastProgressTime = now;
			pushKillEvent(now, 1);
			return;
		}

		long delta = entry.current() - current;
		max = entry.max();

		if (delta <= 0) {
			// Le widget a été re-scanné (polling) mais rien n'a bougé.
			current = entry.current();
			return;
		}

		current = entry.current();
		lastProgressTime = now;
		pushKillEvent(now, delta);
	}

	private void pushKillEvent(Instant now, long amount) {
		recentKills.addLast(new KillEvent(now, amount));
		pruneOldEvents(now);
	}

	private void pruneOldEvents(Instant now) {
		while (!recentKills.isEmpty() && Duration.between(recentKills.peekFirst().timestamp(), now).compareTo(RATE_WINDOW) > 0) {
			recentKills.pollFirst();
		}
	}

	private long totalKillsInWindow() {
		return recentKills.stream().mapToLong(KillEvent::amount).sum();
	}

	public boolean isStale(@NonNull Instant now) {
		return Duration.between(lastProgressTime, now).compareTo(INACTIVITY_TIMEOUT) > 0;
	}

	public boolean isActive() {
		return !recentKills.isEmpty() && recentKills.size() >= MIN_KILLS;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasEnoughDataForStats() {
		return totalKillsInWindow() >= MIN_KILLS_FOR_STATS;
	}

	public int getKillsPerHour(@NonNull Instant now) {
		pruneOldEvents(now);
		if (recentKills.isEmpty() || !hasEnoughDataForStats()) return 0;

		Instant oldest = null;
		if (recentKills.peekFirst() != null) {
			oldest = recentKills.peekFirst().timestamp();
		} // stupide null check "obligé" car IntelliJ ne comprend pas juste au-dessus
		if (oldest == null) return 0;

		long elapsedSeconds = Math.max(Duration.between(oldest, now).getSeconds(), 1);
		long totalKills = totalKillsInWindow();

		return (int) ((totalKills / (double) elapsedSeconds) * 3600.0);
	}

	public Optional<Instant> getEtaInstant(@NonNull Instant now) {
		double rate = getKillsPerHour(now);
		long remaining = Math.max(0, max - current);
		if (rate <= 0 || remaining == 0) return Optional.empty();

		double hours = remaining / rate;
		return Optional.of(now.plusSeconds((long) (hours * 3600)));
	}

	public Component getLineComponent() {
		return lineComponent;
	}
}
