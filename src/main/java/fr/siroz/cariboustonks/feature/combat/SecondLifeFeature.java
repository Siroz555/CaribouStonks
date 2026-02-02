package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.MultiElementHud;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.Pair;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SecondLifeFeature extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_second_life");

	private static final Pattern SPIRIT_MASK_PATTERN = Pattern.compile("Second Wind Activated! Your Spirit Mask saved your life!");
	private static final Pattern BONZO_MASK_PATTERN = Pattern.compile("Your Bonzo's Mask saved your life!");
	private static final Pattern PHOENIX_PET_PATTERN = Pattern.compile("Your Phoenix Pet saved you from certain death!");

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("");
	private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#0.0");

	private final Map<SecondLife, Long> activeCooldowns = new HashMap<>();
	private boolean serverHasChanged = false;
	private final HudElementBuilder hudBuilder;
	private final Hud hud;

	public SecondLifeFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);

		this.hudBuilder = new HudElementBuilder();
		this.hud = new MultiElementHud(
				() -> this.isEnabled() && !activeCooldowns.isEmpty(),
				new HudElementTextBuilder()
						.append(Text.literal("Spirit Mask: 42.9s").formatted(Formatting.DARK_PURPLE))
						.append(Text.literal("Phoenix: 51.7s").formatted(Formatting.YELLOW))
						.build(),
				this::getHudLines,
				ConfigManager.getConfig().combat.secondLife.cooldownHud,
				150,
				50
		);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return hud;
	}

	@Override
	protected void onClientJoinServer() {
		serverHasChanged = true;
		activeCooldowns.clear();
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Text text) {
		if (!isEnabled()) return;

		String message = StonksUtils.stripColor(text.getString());
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);

		if (matcher.usePattern(SPIRIT_MASK_PATTERN).matches()) {
			onSecondLife(SecondLife.SPIRIT_MASK);
			return;
		}

		if (matcher.usePattern(PHOENIX_PET_PATTERN).matches()) {
			onSecondLife(SecondLife.PHOENIX_PET);
			return;
		}

		if (SkyBlockAPI.getIsland() == IslandType.DUNGEON && matcher.usePattern(BONZO_MASK_PATTERN).matches()) {
			onSecondLife(SecondLife.BONZO_MASK);
		}
	}

	private void onSecondLife(@NotNull SecondLife secondLife) {
		// Ce "state" permet d'éviter que le runLater run après un changement de serveur.
		// Il est reset ici pour permettre de trigger à nouveau, tant qu'il n'y a pas de changement de serveur.
		serverHasChanged = false;

		if (secondLife.isUseConfig()) {
			Client.showTitleAndSubtitle(Text.literal(secondLife.getName()).formatted(secondLife.getColor()),
					Text.literal("Used!").formatted(Formatting.RED),
					0, 25, 0);
		}

		long cooldownEndTime = System.currentTimeMillis() + (secondLife.getCooldown() * 1000L);
		activeCooldowns.put(secondLife, cooldownEndTime);

		TickScheduler.getInstance().runLater(
				() -> onSecondLifeBack(secondLife), secondLife.getCooldown(), TimeUnit.SECONDS);
	}

	private void onSecondLifeBack(@NotNull SecondLife secondLife) {
		activeCooldowns.remove(secondLife);
		// Pas de notification si le serveur a changé
		if (!serverHasChanged && secondLife.isBackConfigEnabled()) {
			if (ConfigManager.getConfig().combat.secondLife.backMessage) {
				Client.sendMessageWithPrefix(Text.literal(secondLife.getName() + " is back!").formatted(Formatting.GREEN));
			}
			if (ConfigManager.getConfig().combat.secondLife.backTitle) {
				Client.showTitleAndSubtitle(Text.literal(secondLife.getName()).formatted(secondLife.getColor()),
						Text.literal("Ready!").formatted(Formatting.GREEN),
						0, 25, 0);
			}
			if (ConfigManager.getConfig().combat.secondLife.backSound) {
				Client.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
			}
		}
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		if (activeCooldowns.isEmpty()) {
			return hudBuilder.build();
		}

		try {
			long currentTime = System.currentTimeMillis();
			activeCooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTime);

			// liste triée des cooldowns par temps restant
			List<Map.Entry<SecondLife, Long>> sortedCooldowns = activeCooldowns.entrySet().stream()
					.sorted(Map.Entry.comparingByValue())
					.toList();

			for (Map.Entry<SecondLife, Long> entry : sortedCooldowns) {
				double timeRemaining = (entry.getValue() - currentTime) / 1000.0;
				if (timeRemaining > 0) {
					SecondLife secondLife = entry.getKey();
					String formattedTime = TIME_FORMAT.format(timeRemaining);
					hudBuilder.appendLine(Text.empty()
							.append(Text.literal(secondLife.getName()).formatted(secondLife.getColor()))
							.append(Text.literal(": ").formatted(Formatting.WHITE))
							.append(Text.literal(formattedTime + "s").formatted(getColor(timeRemaining)))
					);
				}
			}
		} catch (Exception ignored) {
		}

		return hudBuilder.build();
	}

	private Formatting getColor(double timeRemaining) {
		if (timeRemaining <= 10) {
			return Formatting.GREEN;
		} else if (timeRemaining <= 30) {
			return Formatting.GOLD;
		} else {
			return Formatting.RED;
		}
	}

	private enum SecondLife {
		SPIRIT_MASK(30, "Spirit Mask", Formatting.DARK_PURPLE,
				() -> ConfigManager.getConfig().combat.secondLife.spiritMaskUsed,
				() -> ConfigManager.getConfig().combat.secondLife.spiritMaskBack),
		BONZO_MASK(180, "Bonzo Mask", Formatting.RED,
				() -> ConfigManager.getConfig().combat.secondLife.bonzoMaskUsed,
				() -> ConfigManager.getConfig().combat.secondLife.bonzoMaskBack),
		PHOENIX_PET(60, "Phoenix Pet", Formatting.YELLOW,
				() -> ConfigManager.getConfig().combat.secondLife.phoenixUsed,
				() -> ConfigManager.getConfig().combat.secondLife.phoenixBack),
		;

		private final int cooldown;
		private final String name;
		private final Formatting color;
		private final BooleanSupplier useConfig;
		private final BooleanSupplier backConfig;

		SecondLife(int cooldown, String name, Formatting color, BooleanSupplier useConfig, BooleanSupplier backConfig) {
			this.cooldown = cooldown;
			this.name = name;
			this.color = color;
			this.useConfig = useConfig;
			this.backConfig = backConfig;
		}

		public int getCooldown() {
			return cooldown;
		}

		public String getName() {
			return name;
		}

		public Formatting getColor() {
			return color;
		}

		public boolean isUseConfig() {
			return useConfig.getAsBoolean();
		}

		public boolean isBackConfigEnabled() {
			return backConfig.getAsBoolean();
		}
	}
}
