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
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class AnimationUtils {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final int RAINBOW_CHANGE_RATE = 10;
	private static Color currentRainbowColor = Colors.RED;

	private AnimationUtils() {
	}

	@ApiStatus.Internal
	public static void initAnimationUtilities() {
		TickScheduler.getInstance().runRepeating(AnimationUtils::onTick, 1);
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
