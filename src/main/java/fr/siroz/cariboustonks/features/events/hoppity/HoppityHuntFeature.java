package fr.siroz.cariboustonks.features.events.hoppity;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockSeason;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
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
import org.jspecify.annotations.NonNull;

public class HoppityHuntFeature extends Feature { // TODO :: CONFIG et final tests
	private static final String CUTE_RABBIT = "\uD83D\uDC07";
	private static final Pattern EGG_FOUND_PATTERN = Pattern.compile("HOPPITY'S HUNT You found a Chocolate (?<eggType>.+?) Egg\\b.*$");
	private static final Pattern EGG_SPAWN_PATTERN = Pattern.compile("HOPPITY'S HUNT A Chocolate (?<eggType>.+?) Egg has appeared!");
	private static final String NO_MORE_EGGS_MESSAGE = "There are no hidden Chocolate Rabbit Eggs nearby! Try again later!";
	private static final long DELTA_DISPLAY_MS = 5_000;
	private static final Map<Integer, List<EggType>> EGG_GROUPS_BY_HOUR = Arrays.stream(EggType.VALUES)
			.collect(Collectors.groupingBy(
					EggType::getResetDay,
					TreeMap::new,
					Collectors.toList()
			)); // Eggs grouped by spawn hour (7, 14, 21), in chronological order.

	private final Map<EggType, EggStatus> eggStates = new EnumMap<>(EggType.class);
	private boolean allAvailableNotified = false;
	private long deltaTimestamp = 0L;

	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	public HoppityHuntFeature() {
		Arrays.stream(EggType.VALUES).forEach(egg -> this.eggStates.put(egg, EggStatus.WAITING));

		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
		SkyBlockEvents.HOUR_CHANGE_EVENT.register(this::onSkyBlockHourChange);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("hoppity_hunt"))
				.hud(new MultiElementHud(
						this::isEnabled, // TODO :: config + hud + season
						new HudElementTextBuilder()
								.append(Component.literal("Hoppity's Hunt Eggs " + CUTE_RABBIT))
								.appendSpace()
								.append(Component.literal("§8○ §7?"))
								.append(Component.literal("§8○ §7?"))
								.append(Component.literal("§8○ §7?"))
								.build(),
						this::getHudLines,
						this.config().uiAndVisuals.deployables.hud,
						150,
						15
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

		EGG_GROUPS_BY_HOUR.getOrDefault(hour, List.of())
				.stream()
				.filter(egg -> egg.isAlternateDay() == isAlternateDay)
				.forEach(egg -> eggStates.put(egg, EggStatus.AVAILABLE));

		checkAndNotifyAllAvailable();
	}

	private void handleEggFound(@NonNull String eggTypeName) {
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
	private void handleEggSpawn(@NonNull String eggTypeName) {
		EggType eggType = EggType.getByName(eggTypeName);
		if (eggType != null) {
			eggStates.put(eggType, EggStatus.AVAILABLE);
			checkAndNotifyAllAvailable();
		}
	}

	private void handleNoMoreEggs() {
		// Force le passage a CLAIMED
		eggStates.replaceAll((_, status) -> status == EggStatus.AVAILABLE ? EggStatus.CLAIMED : status);
		allAvailableNotified = false;
	}

	private void checkAndNotifyAllAvailable() {
		if (allAvailableNotified) return;

		boolean allAvailable = Arrays.stream(EggType.VALUES)
				.allMatch(egg -> eggStates.get(egg) == EggStatus.AVAILABLE);

		if (allAvailable) {
			allAvailableNotified = true;
			deltaTimestamp = System.currentTimeMillis();

			PlayerContext.sendMessageWithPrefix(
					Component.literal(CUTE_RABBIT + " All Hoppity eggs are ready to collect!").withStyle(ChatFormatting.GOLD)
			);
		}
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		String title = "Hoppity's Hunt Eggs " + CUTE_RABBIT;
		boolean showDelta = System.currentTimeMillis() - deltaTimestamp < DELTA_DISPLAY_MS;
		if (showDelta) {
			hudBuilder.appendTitle(AnimationUtils.applyColorCycle(title, 500, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE));
		} else {
			hudBuilder.appendTitle(Component.literal(title).withStyle(ChatFormatting.LIGHT_PURPLE));
		}

		for (EggType egg : EggType.VALUES) {
			EggStatus status = eggStates.getOrDefault(egg, EggStatus.WAITING);

			hudBuilder.appendTableRow(
					Component.literal(status.getSymbol()).withStyle(status.getColor()),
					status == EggStatus.CLAIMED
							? Component.literal(egg.getName()).withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH)
							: Component.literal(egg.getName()).withStyle(egg.getColor()),
					Component.empty()
			);
			hudBuilder.appendSpace();
		}

		return hudBuilder.build();
	}
}
