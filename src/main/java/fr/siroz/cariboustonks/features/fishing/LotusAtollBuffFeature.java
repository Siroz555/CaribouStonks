package fr.siroz.cariboustonks.features.fishing;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.infrastructure.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LotusAtollBuffFeature extends Feature {
	private static final int MAX_BUFFS = 4;
	private static final Duration BUFF_DURATION = Duration.ofMinutes(30);
	private static final Duration EXPIRED_DISPLAY_DURATION = Duration.ofSeconds(10);
	private static final String SEA_CREATURE_BUFF_TEXT = "WISE! You've been granted +2.5α Sea Creature Chance for 30m while on the Lotus Atoll!";
	private static final String FISHING_SPEED_BUFF_TEXT = "WISE! You've been granted +10☂ Fishing Speed for 30m while on the Lotus Atoll!";
	private static final String TROPHY_CHANCE_BUFF_TEXT = "WISE! You've been granted +5♔ Trophy Chance for 30m while on the Lotus Atoll!";
	private static final String TREASURE_CHANCE_BUFF_TEXT = "WISE! You've been granted +1⛃ Treasure Chance for 30m while on the Lotus Atoll!";

	private Instant seaCreatureExpiry = null;
	private Instant fishingSpeedExpiry = null;
	private Instant trophyChanceExpiry = null;
	private Instant treasureChanceExpiry = null;

	private Instant allExpiredAt = null;
	private boolean hasNotifiedExpiry = false;
	private int previousActiveCount = 0;
	private boolean onLotusAtollState = false;
	private boolean inGracePeriod = false;

	public LotusAtollBuffFeature() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
		SkyBlockEvents.ISLAND_CHANGE_EVENT.register(this::onSkyBlockIslandChange);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("lotus_atoll_buffs"))
				.hud(new TextHud(
						Component.literal("§3Lotus Atoll Buff §e3§7/§e4§3: §e25min"),
						this::getHudText,
						this.config().fishing.lotusAtoll.buffHud,
						100,
						100
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && onLotusAtollState;
	}

	@Override
	protected void onSecondPassed() {
		if (!isEnabled()) return;

		int currentActiveBuffs = countActiveBuffs();

		if (!hasNotifiedExpiry && currentActiveBuffs < previousActiveCount) {
			hasNotifiedExpiry = true;
			notifyExpired();
		}

		if (previousActiveCount > 0 && currentActiveBuffs == 0) {
			allExpiredAt = Instant.now();
		}

		previousActiveCount = currentActiveBuffs;

		inGracePeriod = allExpiredAt != null
				&& Instant.now().isBefore(allExpiredAt.plus(EXPIRED_DISPLAY_DURATION));

		if (!isActive(seaCreatureExpiry)) seaCreatureExpiry = null;
		if (!isActive(fishingSpeedExpiry)) fishingSpeedExpiry = null;
		if (!isActive(trophyChanceExpiry)) trophyChanceExpiry = null;
		if (!isActive(treasureChanceExpiry)) treasureChanceExpiry = null;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component component) {
		if (!isEnabled()) return;

		String plain = component.getString();
		Instant expiry = Instant.now().plus(BUFF_DURATION);

		if (seaCreatureExpiry == null && plain.contains(SEA_CREATURE_BUFF_TEXT)) seaCreatureExpiry = expiry;
		if (fishingSpeedExpiry == null && plain.contains(FISHING_SPEED_BUFF_TEXT)) fishingSpeedExpiry = expiry;
		if (trophyChanceExpiry == null && plain.contains(TROPHY_CHANCE_BUFF_TEXT)) trophyChanceExpiry = expiry;
		if (treasureChanceExpiry == null && plain.contains(TREASURE_CHANCE_BUFF_TEXT)) treasureChanceExpiry = expiry;
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE_EVENT")
	private void onSkyBlockIslandChange(@NonNull IslandType islandType, String s) {
		onLotusAtollState = islandType == IslandType.LOTUS_ATOLL;
	}

	private Component getHudText() {
		if (!onLotusAtollState || (!inGracePeriod && !hasOneBuff())) return Component.empty();

		if (inGracePeriod && !hasOneBuff()) {
			return Component.empty()
					.append(Component.literal("Lotus Atoll Buffs ").withStyle(ChatFormatting.DARK_AQUA))
					.append(AnimationUtils.applyColorCycle("EXPIRED", 555, ChatFormatting.RED, ChatFormatting.DARK_RED));
		}

		int active = countActiveBuffs();
		return Component.empty()
				.append(Component.literal("Lotus Atoll Buffs ").withStyle(ChatFormatting.DARK_AQUA))
				.append(Component.literal("" + active).withStyle(active == MAX_BUFFS ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
				.append(Component.literal("/").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(MAX_BUFFS + "").withStyle(ChatFormatting.GREEN))
				.append(Component.literal(": ").withStyle(ChatFormatting.DARK_AQUA))
				.append(Component.literal(generalTime()).withStyle(ChatFormatting.YELLOW));
	}

	private void notifyExpired() {
		TickScheduler.getInstance().runLater(() -> hasNotifiedExpiry = false, 10, TimeUnit.SECONDS);

		if (this.config().fishing.lotusAtoll.buffExpiredWarn) {
			PlayerContext.sendMessageWithPrefix(Component.empty()
					.append(Component.literal("The").withStyle(ChatFormatting.RED))
					.append(Component.literal(" Lotus Atoll ").withStyle(ChatFormatting.DARK_AQUA))
					.append(Component.literal("buffs are about to expire!").withStyle(ChatFormatting.RED))
			);
			PlayerContext.showSubtitle(Component.literal("Buffs!").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), 1, 30, 1);
			PlayerContext.playSound(SoundEvents.ALLAY_HURT, 1f, 1f);
		}
	}

	private int countActiveBuffs() {
		int count = 0;
		if (isActive(seaCreatureExpiry)) count++;
		if (isActive(fishingSpeedExpiry)) count++;
		if (isActive(trophyChanceExpiry)) count++;
		if (isActive(treasureChanceExpiry)) count++;
		return count;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean hasOneBuff() {
		return isActive(seaCreatureExpiry)
				|| isActive(fishingSpeedExpiry)
				|| isActive(trophyChanceExpiry)
				|| isActive(treasureChanceExpiry);
	}

	private boolean isActive(@Nullable Instant expiry) {
		return expiry != null && Instant.now().isBefore(expiry);
	}

	private String generalTime() {
		// Expiry la plus haute parmi les buffs actifs
		return Stream.of(seaCreatureExpiry, fishingSpeedExpiry, trophyChanceExpiry, treasureChanceExpiry)
				.filter(this::isActive) // Check null aussi
				.max(Comparator.naturalOrder()) // expiry la plus haute
				.map(TimeUtils::getDurationFormatted)
				.orElse("???");
	}
}
