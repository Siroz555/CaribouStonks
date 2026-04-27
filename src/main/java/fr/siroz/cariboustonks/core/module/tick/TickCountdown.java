package fr.siroz.cariboustonks.core.module.tick;

/**
 * Composable tick countdown.
 */
public final class TickCountdown {
	private int ticks = 0;

	public TickCountdown() {
	}

	public void set(int ticks) {
		this.ticks = Math.max(0, ticks);
	}

	public void reset() {
		this.ticks = 0;
	}

	public void tick() {
		if (ticks > 0) {
			ticks--;
		}
	}

	public int getTicks() {
		return ticks;
	}

	public boolean isCounting() {
		return ticks > 0;
	}
}
