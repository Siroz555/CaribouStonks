package fr.siroz.cariboustonks.core.mod;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ModFlavor {

	@Nullable
	private SplashRenderer splashText = null;

	private static final List<SplashRenderer> PREDEFINED_SPLASH = List.of(
			new SplashRenderer(Component.literal("/visit Siroz555 ;)").withStyle(ChatFormatting.DARK_AQUA)),
			new SplashRenderer(Component.literal("Crafting like a French baker").withStyle(ChatFormatting.YELLOW)),
			new SplashRenderer(Component.literal("Foraging Update?").withStyle(ChatFormatting.YELLOW)),
			new SplashRenderer(Component.literal("3-5 Business days").withStyle(ChatFormatting.LIGHT_PURPLE)),
			new SplashRenderer(Component.literal("Galatea!").withStyle(ChatFormatting.DARK_GREEN)),
			new SplashRenderer(Component.literal("SkyBlock!").withStyle(ChatFormatting.YELLOW))
	);

	public ModFlavor() {
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

	public @NonNull Optional<SplashRenderer> getSplashText() {
		return Optional.ofNullable(splashText);
	}
}
