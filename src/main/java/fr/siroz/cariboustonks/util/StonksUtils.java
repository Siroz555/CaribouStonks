package fr.siroz.cariboustonks.util;

import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Position;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
	@ApiStatus.Internal
	public static void initUtilities() {
		AnimationUtils.initAnimationUtilities();
		TickScheduler.getInstance().runRepeating(Client::handleUpdates, 1, TimeUnit.SECONDS);
	}

	/**
	 * Permet d'afficher au client le {@link ErrorScreen} avec un titre et le message.
	 *
	 * @param title   le titre
	 * @param message le message
	 */
	public static void showFatalErrorScreen(@NotNull Component title, @NotNull Component message) {
		if (CLIENT.player != null) CLIENT.setScreen(new ErrorScreen(title, message));
	}

	@Contract(pure = true)
	public static @NotNull String stripColor(@Nullable String input) {
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
		String serverBrand = CLIENT.player != null
				&& CLIENT.player.connection != null
				&& CLIENT.player.connection.serverBrand() != null
				? CLIENT.player.connection.serverBrand()
				: "";

		return serverAddress.contains("hypixel.net") || serverBrand.contains("Hypixel BungeeCord");
	}

	public static Optional<String> textToJson(@NotNull Component text) {
		try {
			String json = GsonProvider.standard().toJson(text);
			return Optional.ofNullable(json);
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	public static Optional<Component> jsonToText(@NotNull String json) {
		try {
			Component text = GsonProvider.standard().fromJson(json, Component.class);
			return Optional.ofNullable(text);
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	@Contract("_, _ -> param1")
	public static <T> T make(@NotNull T object, @NotNull Consumer<? super T> initializer) {
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
	public static double calculateMedian(@NotNull List<Double> values) {
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
	 * Créer une commande qui met en file d'attente un écran à ouvrir dans le prochain tick.
	 * Utilisé pour éviter que l'écran ne se ferme immédiatement après l'exécution de la commande.
	 *
	 * @param screenSupplier le supplier de l'écran à ouvrir
	 * @return {@link Command < FabricClientCommandSource >}
	 */
	@Contract(pure = true)
	public static @NotNull Command<FabricClientCommandSource> openScreen(@NotNull Supplier<Screen> screenSupplier) {
		return context -> {
			CLIENT.schedule(() -> CLIENT.setScreen(screenSupplier.get()));
			return Command.SINGLE_SUCCESS;
		};
	}

	/**
	 * Calculates the squared distance between two positions, ignoring their Y coordinates.
	 *
	 * @param from the starting position
	 * @param to   the destination position
	 * @return the squared distance between the two positions
	 */
	public static double squaredDistanceToIgnoringY(@NotNull Position from, @NotNull Position to) {
		double dx = from.x() - to.x();
		double dz = from.z() - to.z();
		return dx * dx + dz * dz;
	}

	/**
	 * Supprime chaque deuxième élément de la liste donnée tout en conservant le premier et le dernier élément.
	 * Si la liste contient moins de trois éléments, elle est retournée inchangée.
	 *
	 * @param list la liste dont chaque deuxième élément doit être supprimé
	 * @return la nouvelle liste après traitement
	 */
	@Deprecated
	public static <T> @NotNull List<T> removeEverySecondElement(@NotNull List<T> list) {
		if (list.size() < 3) return list;

		List<T> result = new ArrayList<>();
		result.add(list.getFirst());

		for (int i = 1; i < list.size() - 1; i++) {
			if (i % 2 == 0) {
				result.add(list.get(i));
			}
		}

		result.add(list.getLast());

		return result;
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
	public static <T> @NotNull List<T> reduceListToApproxSize(@NotNull List<T> list, int targetSize) {
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
	public static String capitalize(@NotNull String s) {
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
