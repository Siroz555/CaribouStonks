package fr.siroz.cariboustonks.features.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;

public class SecondLifeFeature extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_second_life");

	private static final Pattern SPIRIT_MASK_PATTERN = Pattern.compile("Second Wind Activated! Your Spirit Mask saved your life!");
	private static final Pattern BONZO_MASK_PATTERN = Pattern.compile("Your Bonzo's Mask saved your life!");
	private static final Pattern PHOENIX_PET_PATTERN = Pattern.compile("Your Phoenix Pet saved you from certain death!");

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("");
	private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#0.0");

	private final Map<SecondLife, Long> activeCooldowns = new HashMap<>();
	private boolean serverHasChanged = false;
	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	public SecondLifeFeature() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new MultiElementHud(
						() -> this.isEnabled() && !activeCooldowns.isEmpty(),
						new HudElementTextBuilder()
								.append(Component.literal("Spirit Mask: 42.9s").withStyle(ChatFormatting.DARK_PURPLE))
								.append(Component.literal("Phoenix: 51.7s").withStyle(ChatFormatting.YELLOW))
								.build(),
						this::getHudLines,
						this.config().combat.secondLife.cooldownHud,
						150,
						50
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Override
	protected void onClientJoinServer() {
		serverHasChanged = true;
		activeCooldowns.clear();
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component text) {
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

	private void onSecondLife(@NonNull SecondLife secondLife) {
		// Ce "state" permet d'éviter que le runLater run après un changement de serveur.
		// Il est reset ici pour permettre de trigger à nouveau, tant qu'il n'y a pas de changement de serveur.
		serverHasChanged = false;

		if (secondLife.isUseConfig()) {
			Client.showTitleAndSubtitle(Component.literal(secondLife.getName()).withStyle(secondLife.getColor()),
					Component.literal("Used!").withStyle(ChatFormatting.RED),
					0, 25, 0);
		}

		long cooldownEndTime = System.currentTimeMillis() + (secondLife.getCooldown() * 1000L);
		activeCooldowns.put(secondLife, cooldownEndTime);

		TickScheduler.getInstance().runLater(
				() -> onSecondLifeBack(secondLife), secondLife.getCooldown(), TimeUnit.SECONDS);
	}

	private void onSecondLifeBack(@NonNull SecondLife secondLife) {
		activeCooldowns.remove(secondLife);
		// Pas de notification si le serveur a changé
		if (!serverHasChanged && secondLife.isBackConfigEnabled()) {
			if (this.config().combat.secondLife.backMessage) {
				Client.sendMessageWithPrefix(Component.literal(secondLife.getName() + " is back!").withStyle(ChatFormatting.GREEN));
			}
			if (this.config().combat.secondLife.backTitle) {
				Client.showTitleAndSubtitle(Component.literal(secondLife.getName()).withStyle(secondLife.getColor()),
						Component.literal("Ready!").withStyle(ChatFormatting.GREEN),
						0, 25, 0);
			}
			if (this.config().combat.secondLife.backSound) {
				Client.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
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
					hudBuilder.appendLine(Component.empty()
							.append(Component.literal(secondLife.getName()).withStyle(secondLife.getColor()))
							.append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
							.append(Component.literal(formattedTime + "s").withStyle(getColor(timeRemaining)))
					);
				}
			}
		} catch (Exception ex) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.warn("{} Unable to update hud lines", getShortName(), ex);
			}
		}

		return hudBuilder.build();
	}

	private ChatFormatting getColor(double timeRemaining) {
		if (timeRemaining <= 10) {
			return ChatFormatting.GREEN;
		} else if (timeRemaining <= 30) {
			return ChatFormatting.GOLD;
		} else {
			return ChatFormatting.RED;
		}
	}

	private enum SecondLife {
		SPIRIT_MASK(30, "Spirit Mask", ChatFormatting.DARK_PURPLE,
				() -> ConfigManager.getConfig().combat.secondLife.spiritMaskUsed,
				() -> ConfigManager.getConfig().combat.secondLife.spiritMaskBack),
		BONZO_MASK(180, "Bonzo Mask", ChatFormatting.RED,
				() -> ConfigManager.getConfig().combat.secondLife.bonzoMaskUsed,
				() -> ConfigManager.getConfig().combat.secondLife.bonzoMaskBack),
		PHOENIX_PET(60, "Phoenix Pet", ChatFormatting.YELLOW,
				() -> ConfigManager.getConfig().combat.secondLife.phoenixUsed,
				() -> ConfigManager.getConfig().combat.secondLife.phoenixBack),
		;

		private final int cooldown;
		private final String name;
		private final ChatFormatting color;
		private final BooleanSupplier useConfig;
		private final BooleanSupplier backConfig;

		SecondLife(int cooldown, String name, ChatFormatting color, BooleanSupplier useConfig, BooleanSupplier backConfig) {
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

		public ChatFormatting getColor() {
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
