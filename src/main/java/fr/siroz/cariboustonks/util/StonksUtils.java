package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Stonks utilities
 */
public final class StonksUtils {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	/**
	 * {@code §[0-9a-fklmnor]}
	 */
	private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

	/**
	 * => {@code 100,000,000}
	 */
	public static final NumberFormat INTEGER_NUMBERS = NumberFormat.getIntegerInstance(Locale.US);

	/**
	 * => {@code 100,000.15}
	 */
	public static final NumberFormat DOUBLE_NUMBERS = StonksUtils.make(
			NumberFormat.getInstance(Locale.US),
			nf -> nf.setMaximumFractionDigits(2));

	/**
	 * => {@code 100,000.1}
	 */
	public static final NumberFormat FLOAT_NUMBERS = StonksUtils.make(
			NumberFormat.getInstance(Locale.US),
			nf -> nf.setMaximumFractionDigits(1));

	/**
	 * => {@code 10B} / {@code 10M} / {@code 5K}
	 */
	public static final NumberFormat SHORT_INTEGER_NUMBERS = NumberFormat.getCompactNumberInstance(
			Locale.US, NumberFormat.Style.SHORT);

	/**
	 * => {@code 42.6B} / {@code 69.5M} / {@code 10.2K}
	 */
	public static final NumberFormat SHORT_FLOAT_NUMBERS = StonksUtils.make(
			NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT),
			nf -> nf.setMinimumFractionDigits(1));

	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

	private StonksUtils() {
	}

	/**
	 * Init utilities
	 */
	public static void initUtilities() {
		AnimationUtils.initAnimationUtilities();
		TickScheduler.getInstance().runRepeating(Client::handleUpdates, 1, TimeUnit.SECONDS);
	}

	public static @NonNull String stripColor(@Nullable String input) {
		if (input == null || input.isEmpty()) return "";

		return COLOR_CODE_PATTERN.matcher(input).replaceAll("");
	}

	/**
	 * Vérifie si le client est connecté à Hypixel.
	 *
	 * @return {@code true}/ {@code false}
	 */
	public static boolean isConnectedToHypixel() {
		String serverAddress = CLIENT.getCurrentServer() != null
				? CLIENT.getCurrentServer().ip.toLowerCase(Locale.ENGLISH)
				: "";
		String serverBrand = CLIENT.player != null && CLIENT.player.connection.serverBrand() != null
				? CLIENT.player.connection.serverBrand()
				: "";

		if (serverBrand == null) {
			return false;
		}

		return serverAddress.contains("hypixel.net") || serverBrand.contains("Hypixel BungeeCord");
	}

	public static <T> T make(@NonNull T object, @NonNull Consumer<? super T> initializer) {
		initializer.accept(object);
		return object;
	}

	/**
	 * Converts a {@link String} into a {@code int} or return the default value if the conversion fails.
	 *
	 * @param s            the string to convert
	 * @param defaultValue the default value
	 * @return the int represented by the string or the default value
	 */
	public static int toInt(@Nullable String s, int defaultValue) {
		if (s == null) return defaultValue;

		try {
			return Integer.parseInt(s);
		} catch (Exception ignored) {
			return defaultValue;
		}
	}

	/**
	 * The median is the central value of a sorted list. It is less sensitive to extreme values than the means.
	 *
	 * @param values the values
	 * @return the Median
	 */
	public static double calculateMedian(@NonNull List<Double> values) {
		if (values.isEmpty()) return -1;

		List<Double> sorted = values.stream().sorted().toList();

		int n = sorted.size();
		if (n % 2 == 1) {
			return sorted.get(n / 2);
		} else {
			return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
		}
	}

	/**
	 * Calculates the squared distance between two positions, ignoring their Y coordinates.
	 *
	 * @param from the starting position
	 * @param to   the destination position
	 * @return the squared distance between the two positions
	 */
	public static double squaredDistanceToIgnoringY(@NonNull Position from, @NonNull Position to) {
		double dx = from.x() - to.x();
		double dz = from.z() - to.z();
		return dx * dx + dz * dz;
	}

	/**
	 * Checks if a given slot in an inventory is located at the edge.
	 *
	 * @param slotId the slot ID to check, where the slots are numbered sequentially from 0
	 * @param rows   the total number of rows in the inventory
	 * @return {@code true} if the slot is on the edge of the inventory (first or last column, or first or last row)
	 */
	public static boolean isEdgeSlot(int slotId, int rows) {
		if (slotId < 0 || slotId >= rows * 9) return false;
		int row = slotId / 9;
		int col = slotId % 9;
		return col == 0 || col == 8 || row == 0 || row == rows - 1;
	}

	/**
	 * Converts a hotbar index into the corresponding slot index in the player's inventory.
	 *
	 * @param hotbarIndex the index of the hotbar (0-8 inclusive)
	 * @return the slot index in the player's inventory corresponding to the given hotbar index,
	 * or {@code -1} if the provided hotbar index is out of the valid range
	 */
	@SuppressWarnings("unused")
	public static int convertHotbarToSlotIndex(int hotbarIndex) {
		if (hotbarIndex < 0 || hotbarIndex > 8) return -1;

		return 36 + hotbarIndex;
	}

	/**
	 * Réduit la taille de la liste fournie pour qu'elle atteigne approximativement la taille cible spécifiée.
	 * Si la liste contient un nombre d'éléments inférieur ou égal à la taille cible, la liste est retournée inchangée.
	 * Si la taille de la liste est supérieure à la taille cible, les éléments sont sélectionnés à des intervalles
	 * réguliers pour créer une liste réduite d'une taille approximativement égale à celle souhaitée.
	 * Le dernier élément de la liste originale est toujours inclus dans la liste réduite.
	 *
	 * @param list       la liste à réduire
	 * @param targetSize la taille approximative souhaitée de la liste réduite
	 * @return la nouvelle liste après traitement
	 */
	public static <T> @NonNull List<T> reduceListToApproxSize(@NonNull List<T> list, int targetSize) {
		if (list.size() <= targetSize) return list;

		List<T> result = new ArrayList<>();
		int step = list.size() / targetSize;

		for (int i = 0; i < list.size(); i += step) {
			result.add(list.get(i));
		}

		if (!result.contains(list.getLast())) {
			result.add(list.getLast());
		}

		return result;
	}

	/**
	 * Capitalize the given input String.
	 *
	 * <li>blessed -> Blessed</li>
	 * <li>BLESSED -> Blessed</li>
	 * <li>blood_soaked -> Blood Soaked</li>
	 * <li>BLOOD_SOAKED -> Blood Soaked</li>
	 *
	 * @param s the input String
	 * @return the capitalized input String
	 */
	public static String capitalize(@NonNull String s) {
		if (s.isEmpty()) return s;

		String normalized = s.replace('_', ' ');
		return Arrays.stream(normalized.split("\\s+"))
				.filter(token -> !token.isEmpty())
				.map(token -> {
					String lower = token.toLowerCase(Locale.ENGLISH);
					return lower.substring(0, 1).toUpperCase(Locale.ENGLISH) + lower.substring(1);
				})
				.collect(Collectors.joining(" "));
	}
}
