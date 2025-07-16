package fr.siroz.cariboustonks.util.render.animation;

import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.math.MathUtils;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class AnimationUtils {

	// NOTE-ME: Add config for fixed and hard-coded values.

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final int RAINBOW_CHANGE_RATE = 10;
	private static Color currentRainbowColor = Colors.RED;

	private AnimationUtils() {
	}

	@ApiStatus.Internal
	public static void initAnimationUtilities() {
		TickScheduler.getInstance().runRepeating(AnimationUtils::onTick, 1);
	}

	/**
	 * Applies a dynamic rainbow color effect to the given text.
	 * <p>
	 * This method creates un {@link Text} object where each character
	 * of the input string is assigned a different color based on the current epoch time,
	 * producing a smooth and animated rainbow gradient effect.
	 *
	 * @param text the input string
	 * @return un {@link Text} instance with each character colored according to a time-based rainbow gradient
	 */
	public static @NotNull Text applyRainbow(@NotNull String text) {
		MutableText result = Text.empty();
		long time = Util.getEpochTimeMs();
		float speed = 2000F; // 2000 | plus bas = plus rapide
		float spacing = 0.05f; // 0.05f | décalage des couleurs pour le next
		float saturation = 0.8F;
		float brightness = 0.95F;

		for (int i = 0; i < text.length(); i++) {
			float hue = ((time % (int) speed) / speed - i * spacing) % 1.0f;
			// S'assure d’avoir une valeur positive car "speed - i" au lieu de "speed + i"
			// ce qui permet d'inverser la "rotation" du cercle des couleurs. NOTE-ME : À voir dans la config
			hue = (hue + 1.0f) % 1.0f;
			int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
			result.append(Text.literal(String.valueOf(text.charAt(i))).withColor(rgb));
		}

		return result;
	}

	public static Color getCurrentRainbowColor() {
		return currentRainbowColor;
	}

	public static void showSpecialEffect(
			@NotNull ItemStack item,
			@Nullable ParticleEffect particle,
			@Range(from = 1, to = 120) int particleAge,
			@NotNull SoundEvent sound,
			float soundVolume,
			float soundPitch
	) {
		showSpecialEffect(null, item, particle, particleAge, sound, soundVolume, soundPitch);
	}

	public static void showSpecialEffect(
			@Nullable Text title,
			@NotNull ItemStack item,
			@Nullable ParticleEffect particle,
			@Range(from = 1, to = 120) int particleAge,
			@NotNull SoundEvent sound,
			float soundVolume,
			float soundPitch
	) {
		if (CLIENT.player != null && CLIENT.world != null) {
			if (title != null) {
				Client.showTitle(title, 0, 60, 20);
			}

			CLIENT.gameRenderer.showFloatingItem(item);

			if (particle != null) {
				CLIENT.particleManager.addEmitter(CLIENT.player, particle, particleAge);
			}

			CLIENT.player.playSound(sound, soundVolume, soundPitch);
		}
	}

	private static void onTick() {
		int r = currentRainbowColor.r;
		int g = currentRainbowColor.g;
		int b = currentRainbowColor.b;

		if (r > 0 && b == 0) {
			r -= RAINBOW_CHANGE_RATE;
			g += RAINBOW_CHANGE_RATE;
		}
		if (g > 0 && r == 0) {
			g -= RAINBOW_CHANGE_RATE;
			b += RAINBOW_CHANGE_RATE;
		}
		if (b > 0 && g == 0) {
			r += RAINBOW_CHANGE_RATE;
			b -= RAINBOW_CHANGE_RATE;
		}

		r = MathUtils.clamp(r, 0, 255);
		g = MathUtils.clamp(g, 0, 255);
		b = MathUtils.clamp(b, 0, 255);

		currentRainbowColor = new Color(r, g, b, 255);
	}
}
