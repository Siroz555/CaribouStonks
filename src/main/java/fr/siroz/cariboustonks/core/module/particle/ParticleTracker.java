package fr.siroz.cariboustonks.core.module.particle;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ParticleTracker {
	private final ParticlePathPredictor predictor;
	private final long trackingDurationMs;
	private final Predicate<ParticleData> particleFilter;
	private final Consumer<Vec3> onPositionPredicted;
	private final Runnable onTrackingStarted;
	private final Runnable onTrackingReset;
	private final boolean proximityResetOnly;
	private final double proximityThresholdSq;

	private Vec3 predictedPosition;
	private long trackingStartTime;
	private boolean isTracking;
	private boolean canCheckProximity;

	private ParticleTracker(
			ParticlePathPredictor predictor,
			long trackingDurationMs,
			Predicate<ParticleData> particleFilter,
			Consumer<Vec3> onPositionPredicted,
			Runnable onTrackingStarted,
			Runnable onTrackingReset,
			boolean proximityResetOnly,
			double proximityThresholdSq
	) {
		this.predictor = predictor;
		this.trackingDurationMs = trackingDurationMs;
		this.particleFilter = particleFilter;
		this.onPositionPredicted = onPositionPredicted;
		this.onTrackingStarted = onTrackingStarted;
		this.onTrackingReset = onTrackingReset;
		this.proximityResetOnly = proximityResetOnly;
		this.proximityThresholdSq = proximityThresholdSq;
		this.isTracking = false;
		this.canCheckProximity = false;
	}

	/**
	 * Starts tracking particles
	 */
	public void startTracking() {
		reset();
		isTracking = true;
		canCheckProximity = proximityResetOnly;
		trackingStartTime = System.currentTimeMillis();

		if (onTrackingStarted != null) {
			onTrackingStarted.run();
		}
	}

	/**
	 * Processes a particle
	 *
	 * @param particleData the particle data
	 */
	public void handleParticle(@NonNull ParticleData particleData) {
		if (!isTracking) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime - trackingStartTime > trackingDurationMs) {
			if (proximityResetOnly) {
				stopTracking();
			} else {
				reset();
			}
			return;
		}

		if (!particleFilter.test(particleData)) return;

		Vec3 position = particleData.position();

		if (predictor.isEmpty()) {
			predictor.addPoint(position);
			return;
		}

		Vec3 lastPoint = predictor.getLastPoint();
		if (lastPoint == null) return;

		double distance = lastPoint.distanceTo(position);
		if (distance == 0.0D || distance > 3.0D) return;

		predictor.addPoint(position);
		Vec3 solved = predictor.solve();

		if (solved != null) {
			predictedPosition = solved;
			onPositionPredicted.accept(solved);
		}
	}

	/**
	 * Checks if the given {@link Vec3} is close enough to the predicted pos and resets tracking.
	 * <p>
	 * Safe to handle without condition
	 *
	 * @param currentPosition the current pos to check against the predicted target
	 */
	public void checkProximity(@Nullable Vec3 currentPosition) {
		if (currentPosition == null) return;
		if (predictedPosition == null) return;
		if (!canCheckProximity) return;

		double distSq = currentPosition.distanceToSqr(predictedPosition);
		if (distSq <= proximityThresholdSq) {
			reset();
		}
	}

	/**
	 * Resets the tracking state
	 */
	public void reset() {
		stopTracking();
		predictedPosition = null;
		canCheckProximity = false;

		if (onTrackingReset != null) {
			onTrackingReset.run();
		}
	}

	/**
	 * Stops listening to new particles without clearing the predicted position
	 */
	private void stopTracking() {
		predictor.reset();
		isTracking = false;
	}

	@Nullable
	public Vec3 getPredictedPosition() {
		return predictedPosition;
	}

	public boolean isTracking() {
		return isTracking;
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private int predictorPoints = 3;
		private long trackingDurationMs = 5000;
		private Predicate<ParticleData> particleFilter;
		private Consumer<Vec3> onPositionPredicted;
		private Runnable onTrackingStarted;
		private Runnable onTrackingReset;
		private boolean proximityResetOnly = false;
		private double proximityThreshold = 2.0;

		/**
		 * Sets the number of points used for prediction
		 *
		 * @param points number of points (default: 3)
		 */
		public Builder predictor(int points) {
			if (points < 1) throw new IllegalArgumentException("points must be >= 1");
			this.predictorPoints = points;
			return this;
		}

		/**
		 * Sets how long to track particles after activation
		 *
		 * @param durationMs duration in milliseconds (default: 5000)
		 */
		public Builder trackingDuration(long durationMs) {
			if (durationMs < 1) throw new IllegalArgumentException("duration must be >= 1");
			this.trackingDurationMs = durationMs;
			return this;
		}

		/**
		 * Sets the particle filter
		 *
		 * @param filter particle filter predicate
		 */
		public Builder particleFilter(@NonNull Predicate<ParticleData> filter) {
			this.particleFilter = filter;
			return this;
		}

		/**
		 * Sets the callback invoked when a position is predicted
		 *
		 * @param callback callback receiving the predicted position
		 */
		public Builder onPositionPredicted(@NonNull Consumer<Vec3> callback) {
			this.onPositionPredicted = callback;
			return this;
		}

		/**
		 * Sets the callback invoked when tracking starts
		 *
		 * @param callback callback invoked on start
		 */
		public Builder onTrackingStarted(@Nullable Runnable callback) {
			this.onTrackingStarted = callback;
			return this;
		}

		/**
		 * Sets the callback invoked when tracking is reset
		 *
		 * @param callback callback invoked on reset
		 */
		public Builder onTrackingReset(@Nullable Runnable callback) {
			this.onTrackingReset = callback;
			return this;
		}

		/**
		 * If enabled, tracking is reset only when {@link #checkProximity(Vec3)} detects the
		 * position is close enough to the target. The trackingDurationMs timeout is ignored.
		 *
		 * @param enabled {@code true} to use proximity only reset (default: false)
		 */
		public Builder proximityResetOnly(boolean enabled) {
			this.proximityResetOnly = enabled;
			return this;
		}

		/**
		 * Sets the distance threshold (in blocks) used by {@link #checkProximity(Vec3)}.
		 *
		 * @param distance max distance to trigger a reset (default: 2.0)
		 */
		public Builder proximityThreshold(double distance) {
			if (distance <= 0) throw new IllegalArgumentException("distance must be > 0");
			this.proximityThreshold = distance;
			return this;
		}

		@NonNull
		public ParticleTracker build() {
			Objects.requireNonNull(particleFilter, "Particle filter must be set");
			Objects.requireNonNull(onPositionPredicted, "Position prediction callback must be set");

			return new ParticleTracker(
					new ParticlePathPredictor(predictorPoints),
					trackingDurationMs,
					particleFilter,
					onPositionPredicted,
					onTrackingStarted,
					onTrackingReset,
					proximityResetOnly,
					proximityThreshold * proximityThreshold
			);
		}
	}
}
