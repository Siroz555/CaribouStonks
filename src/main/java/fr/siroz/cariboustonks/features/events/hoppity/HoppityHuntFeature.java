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

public class HoppityHuntFeature extends Feature {

	private static final Pattern EGG_FOUND_PATTERN = Pattern.compile("HOPPITY'S HUNT You found a Chocolate (?<eggType>.+?) Egg\\b.*$");
	private static final Pattern EGG_SPAWN_PATTERN = Pattern.compile("HOPPITY'S HUNT A Chocolate (?<eggType>.+?) Egg has appeared!");
	private static final String NO_MORE_EGGS_MESSAGE = "There are no hidden Chocolate Rabbit Eggs nearby! Try again later!";

	/**
	 * Eggs grouped by spawn hour (7, 14, 21), in chronological order.
	 * Each entry holds exactly two {@link EggType}: the regular and alternate variant.
	 */
	private static final Map<Integer, List<EggType>> EGG_GROUPS = Arrays.stream(EggType.VALUES)
			.collect(Collectors.groupingBy(
					EggType::getResetDay,
					TreeMap::new,
					Collectors.toList()
			));

	private final Map<EggType, EggStatus> eggStates = new EnumMap<>(EggType.class);
	private boolean initialized = false;

	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	public HoppityHuntFeature() {
		Arrays.stream(EggType.VALUES).forEach(egg -> this.eggStates.put(egg, EggStatus.WAITING));

		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
		SkyBlockEvents.HOUR_CHANGE_EVENT.register(this::onSkyBlockHourChange);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("hoppity_hunt"))
				.hud(new MultiElementHud(
						this::isEnabled,
						new HudElementTextBuilder()
								.append(Component.literal("TEST"))
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
		return SkyBlockAPI.isOnSkyBlock() /*&& SkyBlockAPI.getSeason() == SkyBlockSeason.SPRING*/;
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
		if (!isEnabled()) {
			initialized = false;
			return;
		}

		// TODO - useless SAUF au début de l'Event, mais a voir je ne sais pas..
		//  Il faudrait possiblement init ça au moment ou le joueur rejoint le SB,
		//  mais en cas de kick..
		// State de départ pour l'heure en cours
		if (!initialized) {
			initializeFromHour(hour);
			initialized = true;
			return;
		}

		// --- Egg Spawn Tick ---
		// L'état précédent n'a aucune importance, même un œuf CLAIM est remplacé par un nouvel œuf,
		// car chaque œuf ne dure que jusqu'à sa prochaine réapparition
		// (toutes les 40m~ dans le temps réel / tous les 2 jours SkyBlock).
		EGG_GROUPS.getOrDefault(hour, List.of()).forEach(egg -> {
			// Simple check dans le cas ou le joueur recup l'Egg avant ce tick,
			// mais "normalement" pas possible...
			/*if (eggStates.getOrDefault(egg, EggStatus.WAITING) != EggStatus.CLAIMED) {
				eggStates.put(egg, EggStatus.AVAILABLE);
			}*/
			eggStates.put(egg, EggStatus.AVAILABLE);
		});
	}

	private void handleEggFound(@NonNull String eggTypeName) {
		EggType eggType = EggType.getByName(eggTypeName);
		if (eggType != null) {
			eggStates.put(eggType, EggStatus.CLAIMED);
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
		}
	}

	private void handleNoMoreEggs() {
		// Force le passage a CLAIMED
		eggStates.replaceAll((_, status) -> status == EggStatus.AVAILABLE ? EggStatus.CLAIMED : status);
	}

	/**
	 * Seeds the egg states based on the current SkyBlock hour, for mid-session startup.
	 * <p>
	 * Tout œuf don't l'heure de spawn ({@link EggType#getResetDay()}) est déjà passée
	 * pour la journée en cours est marqué {@link EggStatus#AVAILABLE};
	 * les autres restent en {@link EggStatus#WAITING}.
	 *
	 * @param currentHour the hour
	 */
	private void initializeFromHour(int currentHour) {
		for (EggType eggType : EggType.VALUES) {
			EggStatus initial = currentHour >= eggType.getResetDay()
					? EggStatus.AVAILABLE
					: EggStatus.WAITING;
			eggStates.put(eggType, initial);
		}
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		EGG_GROUPS.values().stream()
				.flatMap(List::stream)
				.forEach(egg -> {
					EggStatus status = eggStates.getOrDefault(egg, EggStatus.WAITING);

					hudBuilder.appendTableRow(
							Component.literal(status.getSymbol()).withStyle(status.getColor()),
							status == EggStatus.CLAIMED
									? Component.literal(egg.getName()).withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH)
									: Component.literal(egg.getName()).withStyle(egg.getColor()),
							Component.empty()
					);
				});

		return hudBuilder.build();
	}
}
