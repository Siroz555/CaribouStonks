package fr.siroz.cariboustonks.util;

import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
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

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

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

	private StonksUtils() {
	}

	/**
	 * Init utilities
	 */
	@ApiStatus.Internal
	public static void initUtilities() {
		WorldRenderUtils.initRenderUtilities();
		AnimationUtils.initAnimationUtilities();
		TickScheduler.getInstance().runRepeating(StonksUtils::updateUtilities, 1, TimeUnit.SECONDS);
	}

	/**
	 * Met à jour les utilities
	 */
	private static void updateUtilities() {
		ScoreboardUtils.internalUpdate(CLIENT);
		TabListUtils.internalUpdate(CLIENT);
	}

	/**
	 * Permet d'afficher au client le {@link FatalErrorScreen} avec un titre et le message.
	 *
	 * @param title   le titre
	 * @param message le message
	 */
	public static void showFatalErrorScreen(@NotNull Text title, @NotNull Text message) {
		if (CLIENT.player != null) CLIENT.setScreen(new FatalErrorScreen(title, message));
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
		String serverAddress = CLIENT.getCurrentServerEntry() != null
				? CLIENT.getCurrentServerEntry().address.toLowerCase()
				: "";
		String serverBrand = CLIENT.player != null
				&& CLIENT.player.networkHandler != null
				&& CLIENT.player.networkHandler.getBrand() != null
				? CLIENT.player.networkHandler.getBrand()
				: "";

		return serverAddress.contains("hypixel.net") || serverBrand.contains("Hypixel BungeeCord");
	}

	public static Optional<String> textToJson(@NotNull Text text) {
		try {
			String json = GsonProvider.standard().toJson(text);
			return Optional.ofNullable(json);
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	public static Optional<Text> jsonToText(@NotNull String json) {
		try {
			Text text = GsonProvider.standard().fromJson(json, Text.class);
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
	 * Implémentation de l'algorithme de Levenshtein.
	 *
	 * @param s1 entrée
	 * @param s2 candidat
	 * @return retourne la distance calculée entre s1 et s2
	 */
	public static int levenshteinDistance(@NotNull String s1, @NotNull String s2) {
		int[][] dp = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0) { // Insère tous les caractères de la deuxième chaîne
					dp[i][j] = j;
				} else if (j == 0) { // Supprime tous les caractères de la première chaîne
					dp[i][j] = i;
				} else if (s1.charAt(i - 1) == s2.charAt(j - 1)) { // égalité
					dp[i][j] = dp[i - 1][j - 1];
				} else { // Si pas d'égalité, le "coût" est augmenté de 1.
					// Substitution / Insertion / Suppression
					dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
				}
			}
		}
		// Distance calculée entre s1 et s2 (coin inférieur droit)
		return dp[s1.length()][s2.length()];
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
			CLIENT.send(() -> CLIENT.setScreen(screenSupplier.get()));
			return Command.SINGLE_SUCCESS;
		};
	}

	/**
	 * Supprime chaque deuxième élément de la liste donnée tout en conservant le premier et le dernier élément.
	 * Si la liste contient moins de trois éléments, elle est retournée inchangée.
	 *
	 * @param list la liste dont chaque deuxième élément doit être supprimé
	 * @return la nouvelle liste après traitement
	 */
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
}
