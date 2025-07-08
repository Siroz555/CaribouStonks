package fr.siroz.cariboustonks.util.math;

public final class MathUtils {

	private MathUtils() {
	}

	public static float map(float sourceNumber, float fromA, float fromB, float toA, float toB) {
		return lerp(toA, toB, inverseLerp(fromA, fromB, sourceNumber));
	}

	public static float lerp(float start, float end, float delta) {
		return start + (end - start) * delta;
	}

	public static double lerp(double start, double end, double delta) {
		return start + (end - start) * delta;
	}

	public static float inverseLerp(float start, float end, float value) {
		return (value - start) / (end - start);
	}

	public static double inverseLerp(double start, double end, double value) {
		return (value - start) / (end - start);
	}

	public static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static long clamp(long value, long min, long max) {
		return Math.min(Math.max(value, min), max);
	}

	public static float clamp(float value, float min, float max) {
		return value < min ? min : Math.min(value, max);
	}

	public static double clamp(double value, double min, double max) {
		return value < min ? min : Math.min(value, max);
	}

	public static int floor(double d) {
		int i = (int) d;
		return d < (double) i ? i - 1 : i;
	}

	public static int fastFloor(double d) {
		return (int) (d + 1024.0) - 1024;
	}

	public static int ceil(double d) {
		int i = (int) d;
		return d > (double) i ? i + 1 : i;
	}
}
