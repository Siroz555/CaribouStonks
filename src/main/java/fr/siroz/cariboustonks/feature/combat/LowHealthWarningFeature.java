package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LowHealthWarningFeature extends Feature {

	private static final Pattern HEALTH_ACTION_BAR_PATTERN = Pattern.compile(
			"§[6c](?<health>[\\d,]+)/(?<max>[\\d,]+)❤ *(?<healing>\\+§c([\\d,]+). *)?");

	private Health currentHealth = Health.DEFAULT;
	private boolean triggered = false;

	public LowHealthWarningFeature() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowActionBar);
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

			if (currentHealth.value <= threshold) {
				triggerLowHealthWarning();
			} else {
				if (triggered) {
					remove();
				}

				triggered = false;
			}
		} catch (Exception ignored) { // valeurs négatives ou max incorrect, techniquement useless ?
		}
	}

	private void triggerLowHealthWarning() {
		if (triggered) return;
		triggered = true;
		show();
	}

	private void show() {
		if (CLIENT.player == null) return;

		// C'est incroyable de faire comme ça, mais bon ça marche lul
		WorldBorder worldBorder = CLIENT.player.getEntityWorld().getWorldBorder();
		worldBorder.setSize(1D);
		worldBorder.setCenter(CLIENT.player.getX() + 5_555, CLIENT.player.getZ() + 5_555);
	}

	private void remove() {
		if (CLIENT.player == null) return;
		CLIENT.player.getEntityWorld().getWorldBorder().load(WorldBorder.Properties.DEFAULT);
	}

	private record Health(int value, int max, int overflow) {
		public static final Health DEFAULT = new Health(100, 100, 0);
	}
}
