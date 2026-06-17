package fr.siroz.cariboustonks.features.events.hoppity;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockSeason;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockTime;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HoppityHuntFeature extends Feature {
	private static final String CUTE_RABBIT = "\uD83D\uDC07";
	private static final Pattern EGG_FOUND_PATTERN = Pattern.compile("HOPPITY'S HUNT You found a Chocolate (?<eggType>.+?) Egg\\b.*$");
	private static final Pattern EGG_SPAWN_PATTERN = Pattern.compile("HOPPITY'S HUNT A Chocolate (?<eggType>.+?) Egg has appeared!");
	private static final String NO_MORE_EGGS_MESSAGE = "There are no hidden Chocolate Rabbit Eggs nearby! Try again later!";
	private static final long DELTA_DISPLAY_MS = 15_000;

	private final Map<EggType, EggStatus> eggStates = new EnumMap<>(EggType.class);
	private final Map<Integer, List<EggType>> eggGroupsByHour = Arrays.stream(EggType.VALUES)
			.collect(Collectors.groupingBy(
					EggType::getResetDay,
					TreeMap::new,
					Collectors.toList()
			)); // Œufs triés par groupe de spawn d'heure (7, 14, 21), dans l'ordre
	private boolean allAvailableNotified = false;
	private long deltaTimestamp = 0L;

	public HoppityHuntFeature() {
		Arrays.stream(EggType.VALUES).forEach(egg -> this.eggStates.put(egg, EggStatus.WAITING));

		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
		SkyBlockEvents.HOUR_CHANGE_EVENT.register(this::onSkyBlockHourChange);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("hoppity_hunt"))
				.hud(new MultiElementHud(
						() -> this.isEnabled() && this.config().events.hoppityHunt.huntHud.showHud,
						new HudElementTextBuilder()
								.append(Component.literal("Hoppity's Hunt Eggs " + CUTE_RABBIT).withStyle(ChatFormatting.LIGHT_PURPLE))
								.appendSpace()
								.append(Component.literal("§8○ §7?"))
								.append(Component.literal("§8○ §7?"))
								.append(Component.literal("§8○ §7?"))
								.build(),
						this::getHudLines,
						this.config().events.hoppityHunt.huntHud,
						150,
						50
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && SkyBlockAPI.getSeason() == SkyBlockSeason.SPRING;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component component) {
		if (!isEnabled()) return;

		String message = component.getString();

		Matcher foundMatcher = EGG_FOUND_PATTERN.matcher(message);
		if (foundMatcher.matches()) {
			handleEggFound(foundMatcher.group("eggType"));
			return;
		}

		Matcher spawnMatcher = EGG_SPAWN_PATTERN.matcher(message);
		if (spawnMatcher.matches()) {
			handleEggSpawn(spawnMatcher.group("eggType"));
			return;
		}

		if (message.contains(NO_MORE_EGGS_MESSAGE)) {
			handleNoMoreEggs();
		}
	}

	@EventHandler(event = "SkyBlockEvents.HOUR_CHANGE_EVENT")
	private void onSkyBlockHourChange(int hour) {
		if (!isEnabled()) return;

		boolean isAlternateDay = (SkyBlockAPI.getTime().day() % 2 == 0);

		eggGroupsByHour.getOrDefault(hour, List.of())
				.stream()
				.filter(egg -> egg.isAlternateDay() == isAlternateDay)
				.forEach(egg -> eggStates.put(egg, EggStatus.AVAILABLE));

		checkAndNotifyAllAvailable();
	}

	private void handleEggFound(@Nullable String eggTypeName) {
		EggType eggType = EggType.getByName(eggTypeName);
		if (eggType != null) {
			eggStates.put(eggType, EggStatus.CLAIMED);
			allAvailableNotified = false;
		}
	}

	/**
	 * Marks the egg as {@link EggStatus#AVAILABLE} when a spawn is received.
	 * <p>
	 * Cela sert de sécu dans les cas où la transition horaire n'a pas pu être effectuée
	 * (si le joueur vient de se connecter ou autre...).
	 * Un message de spawn correspond toujours à un nouvel œuf.
	 * L'état CLAIMED précédent est donc intentionnellement remplacé.
	 *
	 * @param eggTypeName the raw egg type string extracted from the chat message
	 */
	private void handleEggSpawn(@Nullable String eggTypeName) {
		EggType eggType = EggType.getByName(eggTypeName);
		if (eggType != null) {
			eggStates.put(eggType, EggStatus.AVAILABLE);
			checkAndNotifyAllAvailable();
		}
	}

	private void handleNoMoreEggs() {
		// Force le passage a CLAIMED
		eggStates.replaceAll((_, _) -> EggStatus.CLAIMED);
		allAvailableNotified = false;
	}

	private void checkAndNotifyAllAvailable() {
		if (allAvailableNotified) return;

		boolean allAvailable = Arrays.stream(EggType.VALUES)
				.allMatch(egg -> eggStates.get(egg) == EggStatus.AVAILABLE);
		if (!allAvailable) return;

		allAvailableNotified = true;
		deltaTimestamp = System.currentTimeMillis();

		if (this.config().events.hoppityHunt.huntNotification) {
			PlayerContext.sendMessageWithPrefix(Component.empty()
					.append(Component.literal(CUTE_RABBIT).withStyle(ChatFormatting.LIGHT_PURPLE))
					.append(Component.literal(" All Hoppity Eggs are ready to collect!").withStyle(ChatFormatting.GREEN))
			);
			PlayerContext.showTitleAndSubtitle(
					Component.literal(CUTE_RABBIT).withStyle(ChatFormatting.LIGHT_PURPLE),
					Component.literal("Hoppity Eggs are ready!").withStyle(ChatFormatting.GREEN),
					1, 55, 1
			);
			PlayerContext.playSound(SoundEvents.RABBIT_HURT, 10f, 1f);
		}
	}

	private void getHudLines(HudElementBuilder builder) {
		String title = "Hoppity's Hunt Eggs " + CUTE_RABBIT;
		boolean showDelta = System.currentTimeMillis() - deltaTimestamp < DELTA_DISPLAY_MS;
		if (showDelta) {
			builder.appendTitle(AnimationUtils.applyColorCycle(title, 500, TextColor.LIGHT_PURPLE, TextColor.DARK_PURPLE));
		} else {
			builder.appendTitle(Component.literal(title).withStyle(ChatFormatting.LIGHT_PURPLE));
		}

		for (EggType egg : EggType.VALUES) {
			EggStatus status = eggStates.getOrDefault(egg, EggStatus.WAITING);

			Instant nextSpawn = computeNextSpawn(egg);
			String countdown = TimeUtils.getDurationFormatted(Instant.now(), nextSpawn, false);
			Component timeLeft = Component.literal(" " + countdown).withStyle(ChatFormatting.YELLOW);

			builder.appendTableRow(
					Component.literal(status.getSymbol()).withStyle(status.getColor()),
					status == EggStatus.CLAIMED
							? Component.empty()
							  .append(Component.literal(egg.getName()).withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH))
							  .append(timeLeft)
							: Component.empty()
							  .append(Component.literal(egg.getName()).withStyle(egg.getColor()))
							  .append(timeLeft),
					Component.empty()
			);
			builder.appendSpace();
		}
	}

	private Instant computeNextSpawn(@NonNull EggType egg) {
		long currentSkyBlockMillis = SkyBlockAPI.getSkyBlockMillis();
		long posInDay = currentSkyBlockMillis % SkyBlockTime.DAY_MILLIS;
		long spawnInDay = egg.getResetDay() * SkyBlockTime.HOUR_MILLIS;
		long dayIndex = currentSkyBlockMillis / SkyBlockTime.DAY_MILLIS;

		// Parity mapping (day = 1-based avec SkyBlockTime, dayIndex = 0-based):
		// - dayIndex even (0, 2, 4…) <-> day odd (1, 3, 5...) <-> alternateDay=false
		// - dayIndex odd (1, 3, 5…) <-> day even (2, 4, 6...) <-> alternateDay=true
		boolean needsAlternate = egg.isAlternateDay();
		boolean currentMatchesParity = (dayIndex % 2 == 0) == !needsAlternate;
		long millisUntilSpawn = getMillisUntilSpawn(currentMatchesParity, posInDay, spawnInDay);

		return Instant.now().plusMillis(millisUntilSpawn);
	}

	private long getMillisUntilSpawn(boolean currentMatchesParity, long posInDay, long spawnInDay) {
		long millisUntilSpawn;
		if (currentMatchesParity && posInDay < spawnInDay) {
			// Les matchs de parité et l'heure de réapparition n'ont pas encore eu lieu aujourd'hui
			millisUntilSpawn = spawnInDay - posInDay;
		} else if (!currentMatchesParity) {
			// La parité ne correspond pas | demain
			millisUntilSpawn = (SkyBlockTime.DAY_MILLIS - posInDay) + spawnInDay;
		} else {
			// La parité est atteinte, mais l'heure d'apparition est déjà passée aujourd'hui | dans 2 jours
			millisUntilSpawn = (SkyBlockTime.DAY_MILLIS - posInDay) + SkyBlockTime.DAY_MILLIS + spawnInDay;
		}
		return millisUntilSpawn;
	}
}
