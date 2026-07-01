package fr.siroz.cariboustonks.features.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigValue;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.ColorUtils;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

public class LowHealthWarningFeature extends Feature {
	private static final Pattern HEALTH_ACTION_BAR_PATTERN = Pattern.compile(
			"(?<health>[\\d,]+)/(?<max>[\\d,]+)[❤\uE010](?<healing>\\+([\\d,]+). *)?"
	);

	private static final float SPEED = 2f;
	private static final int MAX_ALPHA = 200;
	private static final int MAX_THICKNESS = 120;

	private final ConfigValue<Double> configIntensity = ConfigValue.of(
			() -> this.config().combat.lowHealthWarning.lowHealthWarningIntensity
	);

	private Health currentHealth = Health.DEFAULT;
	private boolean triggered = false;

	public LowHealthWarningFeature() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowActionBar);
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				CaribouStonks.identifier("low_health_overlay"),
				(guiGraphics, _) -> this.renderOverlay(guiGraphics)
		);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.THE_RIFT
				&& this.config().combat.lowHealthWarning.lowHealthWarningEnabled;
	}

	@EventHandler(event = "ClientReceiveMessageEvents.ALLOW_GAME")
	private boolean allowActionBar(Component text, boolean overlay) {
		if (overlay && isEnabled()) {
			Matcher healthActionBarMatcher = HEALTH_ACTION_BAR_PATTERN.matcher(StonksUtils.stripColor(text.getString()));
			if (healthActionBarMatcher.find()) {
				updateHealth(healthActionBarMatcher);
			}
		}

		return true;
	}

	@Override
	protected void onClientTick() {
		if (isEnabled() && MINECRAFT.player != null && MINECRAFT.level != null) {
			updateHealth(currentHealth.value(), currentHealth.max(), currentHealth.overflow());
		}
	}

	private void renderOverlay(GuiGraphicsExtractor guiGraphics) {
		if (!triggered) return;

		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();

		int thickness = (int) (MAX_THICKNESS * configIntensity.get());
		thickness = Math.clamp(thickness, 8, Math.min(width, height) / 2);

		double currentTime = Util.getMillis() / 1000.0D;
		float lerpedAmount = Math.abs(Mth.sin((float) (currentTime * SPEED)));

		int alpha = (int) (MAX_ALPHA * configIntensity.get());

		for (int i = 0; i < thickness; i++) {
			float t = 1.0f - ((float) i / (float) thickness); // 1.0 sur les bords → 0.0 vers le centre
			int a = (int) (alpha * t);
			int lerpedColor = (a << 24) | ColorUtils.lerpRGB(0x00FF0000, 0x00A40000, lerpedAmount);

			guiGraphics.fill(0, i, width, i + 1, lerpedColor); // top
			guiGraphics.fill(0, height - i - 1, width, height - i, lerpedColor); // bottom
			guiGraphics.fill(i, 0, i + 1, height, lerpedColor); // left
			guiGraphics.fill(width - i - 1, 0, width - i, height, lerpedColor); // right
		}
	}

	private void updateHealth(@NonNull Matcher matcher) {
		try {
			int health = Integer.parseInt(matcher.group("health").replace(",", ""));
			int max = Integer.parseInt(matcher.group("max").replace(",", ""));
			updateHealth(health, max, Math.max(0, health - max));
		} catch (Exception ex) { // Si le format change
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.warn("{} Unable to parse health. Format changed?", getShortName(), ex);
			}
		}
	}

	private void updateHealth(int value, int max, int overflow) {
		if (MINECRAFT.player != null) {
			value = (int) (MINECRAFT.player.getHealth() * max / MINECRAFT.player.getMaxHealth());
			overflow = (int) (MINECRAFT.player.getAbsorptionAmount() * max / MINECRAFT.player.getMaxHealth());
		}

		currentHealth = new Health(Math.min(value, max), max, Math.min(overflow, max));
		lowHealthCheck();
	}

	private void lowHealthCheck() {
		try {
			int configThreshold = this.config().combat.lowHealthWarning.lowHealthWarningThreshold;
			int threshold = (int) (currentHealth.max() * (configThreshold / 100.0D));

			triggered = currentHealth.value() <= threshold;
		} catch (Exception _) { // valeurs négatives ou max incorrect, techniquement useless ?
		}
	}

	private record Health(int value, int max, int overflow) {
		public static final Health DEFAULT = new Health(100, 100, 0);
	}
}
