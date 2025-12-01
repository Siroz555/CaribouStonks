package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.colors.ColorUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LowHealthWarningFeature extends Feature {

	private static final Pattern HEALTH_ACTION_BAR_PATTERN = Pattern.compile(
			"§[6c](?<health>[\\d,]+)/(?<max>[\\d,]+)❤ *(?<healing>\\+§c([\\d,]+). *)?");

	private static final float SPEED = 2f;

	private static final int MAX_ALPHA = 200;
	private static final int MAX_THICKNESS = 120;

	private Health currentHealth = Health.DEFAULT;
	private boolean triggered = false;

	public LowHealthWarningFeature() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowActionBar);
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				CaribouStonks.identifier("low_health_overlay"),
				(drawContext, _rtc) -> this.renderOverlay(drawContext)
		);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.THE_RIFT
				&& ConfigManager.getConfig().combat.lowHealthWarning.lowHealthWarningEnabled;
	}

	@EventHandler(event = "ClientReceiveMessageEvents.ALLOW_GAME")
	private boolean allowActionBar(Text text, boolean overlay) {
		if (overlay) { // pour 'forcer' c'est pour toujours retourner true sans avoir d'alerte
			if (isEnabled()) {
				Matcher healthActionBarMatcher = HEALTH_ACTION_BAR_PATTERN.matcher(text.getString());
				if (healthActionBarMatcher.find()) {
					updateHealth(healthActionBarMatcher);
				}
			}
		}

		return true;
	}

	@Override
	protected void onClientTick() {
		if (!isEnabled() || CLIENT.player == null || CLIENT.world == null) {
			return;
		}

		updateHealth(currentHealth.value, currentHealth.max, currentHealth.overflow);
	}

	private void renderOverlay(DrawContext context) {
		if (!triggered) return;

		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();

		int thickness = (int) (MAX_THICKNESS * getIntensity());
		thickness = Math.max(8, Math.min(thickness, Math.min(width, height) / 2));

		double currentTime = Util.getEpochTimeMs() / 1000.0;
		float lerpedAmount = Math.abs(MathHelper.sin((float) (currentTime * SPEED)));

		int alpha = (int) (MAX_ALPHA * getIntensity());

		for (int i = 0; i < thickness; i++) {
			float t = 1.0f - ((float) i / (float) thickness); // 1.0 sur les bords → 0.0 vers le centre
			int a = (int) (alpha * t);
			int lerpedColor = (a << 24) | ColorUtils.lerpRGB(0x00FF0000, 0x00A40000, lerpedAmount);

			context.fill(0, i, width, i + 1, lerpedColor); // top
			context.fill(0, height - i - 1, width, height - i, lerpedColor); // bottom
			context.fill(i, 0, i + 1, height, lerpedColor); // left
			context.fill(width - i - 1, 0, width - i, height, lerpedColor); // right
		}
	}

	private double getIntensity() {
		return ConfigManager.getConfig().combat.lowHealthWarning.lowHealthWarningIntensity;
	}

	private void updateHealth(@NotNull Matcher matcher) {
		try {
			int health = Integer.parseInt(matcher.group("health").replace(",", ""));
			int max = Integer.parseInt(matcher.group("max").replace(",", ""));
			updateHealth(health, max, Math.max(0, health - max));
		} catch (Exception ignored) { // Si le format change
		}
	}

	private void updateHealth(int value, int max, int overflow) {
		if (CLIENT.player != null) {
			value = (int) (CLIENT.player.getHealth() * max / CLIENT.player.getMaxHealth());
			overflow = (int) (CLIENT.player.getAbsorptionAmount() * max / CLIENT.player.getMaxHealth());
		}

		currentHealth = new Health(Math.min(value, max), max, Math.min(overflow, max));
		lowHealthCheck();
	}

	private void lowHealthCheck() {
		try {
			int configThreshold = ConfigManager.getConfig().combat.lowHealthWarning.lowHealthWarningThreshold;
			int threshold = (int) (currentHealth.max * (configThreshold / 100.0D));

			triggered = currentHealth.value <= threshold;
		} catch (Exception ignored) { // valeurs négatives ou max incorrect, techniquement useless ?
		}
	}

	private record Health(int value, int max, int overflow) {
		public static final Health DEFAULT = new Health(100, 100, 0);
	}
}
