package fr.siroz.cariboustonks.util.render.ui;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SplashTextSupplier {

	private static SplashTextSupplier instance;

	@Nullable
	private SplashTextRenderer splashText = null;

	private static final List<SplashTextRenderer> PREDEFINED_SPLASH = List.of(
			new SplashTextRenderer("/visit Siroz555"),
			new SplashTextRenderer("Crafting like a French baker"),
			new SplashTextRenderer("Foraging Update?"),
			new SplashTextRenderer("3-5 Business days"),
			new SplashTextRenderer("Galatea!"),
			new SplashTextRenderer("People want to stay in 1.8 when the latest versions are better for SkyBlock ;'("),
			new SplashTextRenderer("SkyBlock!")
	);

	private SplashTextSupplier() {
		Calendar today = Calendar.getInstance();
		int day = today.get(Calendar.DAY_OF_MONTH);
		int month = today.get(Calendar.MONTH);

		if (day == 11 && month == Calendar.JUNE) {
			this.splashText = new SplashTextRenderer("Happy birthday SkyBlock!");
			return;
		}

		Random random = new Random();
		if (random.nextInt(50) == 0) {
			this.splashText = PREDEFINED_SPLASH.get(random.nextInt(PREDEFINED_SPLASH.size()));
		}
	}

	public static SplashTextSupplier getInstance() {
		return instance == null ? instance = new SplashTextSupplier() : instance;
	}

	@Contract(pure = true)
	public @NotNull Optional<SplashTextRenderer> get() {
		return Optional.ofNullable(splashText);
	}
}
