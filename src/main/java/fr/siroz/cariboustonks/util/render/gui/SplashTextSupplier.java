package fr.siroz.cariboustonks.util.render.gui;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SplashTextSupplier {

	private static SplashTextSupplier instance;

	@Nullable
	private SplashRenderer splashText = null;

	private static final List<SplashRenderer> PREDEFINED_SPLASH = List.of(
			new SplashRenderer(Component.literal("/visit Siroz555")),
			new SplashRenderer(Component.literal("Crafting like a French baker")),
			new SplashRenderer(Component.literal("Foraging Update?")),
			new SplashRenderer(Component.literal("3-5 Business days")),
			new SplashRenderer(Component.literal("Galatea!")),
			new SplashRenderer(Component.literal("People want to stay in 1.8 when the latest versions are better for SkyBlock ;'(")),
			new SplashRenderer(Component.literal("SkyBlock!"))
	);

	private SplashTextSupplier() {
		Calendar today = Calendar.getInstance();
		int day = today.get(Calendar.DAY_OF_MONTH);
		int month = today.get(Calendar.MONTH);

		if (day == 11 && month == Calendar.JUNE) {
			this.splashText = new SplashRenderer(Component.literal("Happy birthday SkyBlock!"));
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
	public @NotNull Optional<SplashRenderer> get() {
		return Optional.ofNullable(splashText);
	}
}
