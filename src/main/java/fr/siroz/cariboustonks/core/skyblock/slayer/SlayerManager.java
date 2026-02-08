package fr.siroz.cariboustonks.core.skyblock.slayer;

import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Mayor;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Perk;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Master Manager regarding Slayers.
 * <p>
 * Allows detecting, manage, and call various events related to Slayer Quests.
 *
 * @see SlayerType
 * @see SlayerTier
 * @see SkyBlockEvents#SLAYER_BOSS_SPAWN_EVENT
 * @see SkyBlockEvents#SLAYER_MINIBOSS_SPAWN_EVENT
 * @see SkyBlockEvents#SLAYER_QUEST_START_EVENT
 * @see SkyBlockEvents#SLAYER_QUEST_FAIL_EVENT
 */
public final class SlayerManager {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Atoned Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend");
	private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
	private static final String QUEST_CANCELLED = "Your Slayer Quest has been cancelled!";
	private static final String QUEST_FAILED = "SLAYER QUEST FAILED!";
	private static final String QUEST_STARTED = "SLAYER QUEST STARTED!";
	private static final String BOSS_SLAIN = "NICE! SLAYER BOSS SLAIN!";
	private static final String QUEST_COMPLETE = "SLAYER QUEST COMPLETE!";
	private static final String SCOREBOARD_BOSS_SPAWNED = "Slay the boss!";

	private SlayerBossFight bossFight;
	private SlayerQuest quest;

	public SlayerManager() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onMessage);
		SkyBlockEvents.ISLAND_CHANGE_EVENT.register(this::onIslandChangeHandler);
		ClientEvents.SCOREBOARD_UPDATE_EVENT.register(this::onScoreboardUpdate);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
	}

	/**
	 * Check if the player is in a Slayer Quest.
	 *
	 * @return {@code true} if a Slayer Quest is active
	 * @see #isBossSpawned()
	 */
	public boolean isInQuest() {
		return quest != null;
	}

	/**
	 * Check if the player is in a Slayer Quest with its Boss spawned.
	 *
	 * @return {@code true} if a Slayer Quest is active and its boss spawned
	 */
	public boolean isBossSpawned() {
		return isInQuest() && bossFight != null;
	}

	/**
	 * Check if the player is in a Slayer Quest with the specified {@link SlayerTier}.
	 *
	 * @param tier the slayer tier
	 * @return {@code true} if a Slayer Quest is active and the slayer tier matches
	 */
	public boolean isSlayerTier(@NonNull SlayerTier tier) {
		return quest != null && quest.getSlayerTier() == tier;
	}

	/**
	 * Check if the player is in Slayer Quest, according to the specified {@link SlayerType} with Boss spawn.
	 *
	 * @param slayerType the slayer type
	 * @return {@code true} if the boss has appeared, a Slayer Quest is active, and the slayer type matches
	 */
	public boolean isInQuestWithBoss(SlayerType slayerType) {
		return isBossSpawned() && quest != null && quest.getSlayerType().equals(slayerType);
	}

	/**
	 * Check if the player is in Slayer Quest, according to the specified {@link SlayerType} without Boss spawn.
	 *
	 * @param slayerType the slayer type
	 * @return {@code true} if the boss has not spawned, a Slayer Quest is active, and the slayer type matches
	 */
	public boolean isInQuestTypeWithoutBoss(@NonNull SlayerType slayerType) {
		return !isBossSpawned() && quest != null && quest.getSlayerType().equals(slayerType);
	}

	/**
	 * Returns a List of {@link Entity} representing the Minibosses during the player's quest.
	 *
	 * @return the entity Minibosses list or an empty list
	 */
	@NonNull
	public List<Entity> getMinibosses() {
		return quest != null ? quest.getMinibosses() : List.of();
	}

	/**
	 * Gets the {@link Entity} representing the Slayer Boss.
	 *
	 * @return the boss entity or null if no Boss Fight is active
	 */
	@Nullable
	public Entity getBossEntity() {
		return bossFight != null ? bossFight.getBossEntity() : null;
	}

	/**
	 * Gets the {@link ArmorStand} representing the Slayer Boss.
	 *
	 * @return the boss armorstand or null if no Boss Fight is active
	 */
	@Nullable
	public ArmorStand getBossArmorStand() {
		return bossFight != null ? bossFight.getBossArmorStand() : null;
	}

	/**
	 * Returns the {@code XP} reward from the given slayer type/tier.
	 * The reward change if the Mayor Aatrox with the "Slayer XP Buff" (x 1.25) is present.
	 *
	 * @param type the type
	 * @param tier the tier
	 * @return the final xp reward
	 */
	public double getXpReward(@NonNull SlayerType type, @NonNull SlayerTier tier) {
		double xp = type.getExpPerTier()[tier.ordinal() - 1]; // -1 car UNKNOWN est en premier
		if (SkyBlockAPI.isMayorOrMinister(Mayor.AATROX, Perk.SLAYER_XP_BUFF)) {
			xp *= 1.25f;
		}

		return xp;
	}

	/**
	 * The main entry point for managing the various statuses of the Slayer Quest's progress.
	 */
	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onMessage(@NonNull Component text) {
		if (!SkyBlockAPI.isOnSkyBlock()) return;

		String message = text.getString();
		message = message.replaceFirst("^\\s+", "");

		switch (message) {
			case QUEST_CANCELLED, QUEST_FAILED -> {
				if (quest != null) {
					SkyBlockEvents.SLAYER_QUEST_FAIL_EVENT.invoker().onFail(quest.getSlayerType(), quest.getSlayerTier());
				}
				quest = null;
				bossFight = null;
			}
			case QUEST_STARTED -> {
				if (quest == null) {
					quest = new SlayerQuest(this);
				}
				SkyBlockEvents.SLAYER_QUEST_START_EVENT.invoker().onStart(quest.getSlayerType(), quest.getSlayerTier(), false);
				bossFight = null;
			}
			case BOSS_SLAIN -> {
				if (quest != null && bossFight != null) {
					bossFight.setSlain(true);
					SkyBlockEvents.SLAYER_BOSS_END_EVENT.invoker().onEnd(quest.getSlayerType(), quest.getSlayerTier(), bossFight.getBossSpawnTime());
				}
			}
			case QUEST_COMPLETE -> {
				if (quest != null && bossFight != null && !bossFight.isSlain()) {
					SkyBlockEvents.SLAYER_BOSS_END_EVENT.invoker().onEnd(quest.getSlayerType(), quest.getSlayerTier(), bossFight.getBossSpawnTime());
				}
				bossFight = null;
			}
			default -> {
			}
		}
	}

	/**
	 * Allows resetting the manager status and attempt to update the information about the active quest
	 * if it is available upon arrival on the server.
	 */
	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE_EVENT")
	private void onIslandChangeHandler(@NonNull IslandType islandType) {
		bossFight = null;
		quest = null;
		TickScheduler.getInstance().runLater(
				() -> updateSlayerBossInfo(false, Client.getScoreboard()),
				3, TimeUnit.SECONDS
		);
	}

	/**
	 * Retrieves the different lines of the scoreboard every second
	 */
	@EventHandler(event = "HudEvents.SCOREBOARD_UPDATE_EVENT")
	private void onScoreboardUpdate(@NonNull List<String> lines) {
		updateSlayerBossInfo(true, lines);
	}

	/**
	 * Check whether the specified ArmorStand is intended for the Boss or Minibosses,
	 * enabling centralized management of the various functionalities' logic.
	 *
	 * @param armorStand the armorstand
	 * @param equipment  no equip packet
	 */
	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NonNull ArmorStand armorStand, boolean equipment) {
		if (!SkyBlockAPI.isOnSkyBlock() || equipment) return;
		if (quest == null || !armorStand.hasCustomName() || (isBossSpawned() && bossFight.getBossEntity() != null)) return;

		if (armorStand.getName().getString().contains(CLIENT.getUser().getName())) {
			for (Entity otherArmorStands : getArmorStands(armorStand)) {
				Matcher slayerMatcher = SLAYER_PATTERN.matcher(otherArmorStands.getName().getString());
				if (slayerMatcher.find()) {
					if (bossFight != null && bossFight.getBossEntity() == null) {
						bossFight.tryToFindBoss((ArmorStand) otherArmorStands);
						return;
					}

					bossFight = new SlayerBossFight(this, (ArmorStand) otherArmorStands);
					return;
				}
			}
		}

		if (CLIENT.player != null && !armorStand.closerThan(CLIENT.player, 20)) {
			return;
		}

		for (SlayerType type : SlayerType.values()) {
			for (String minibossName : type.getMinibossNames()) {
				if (armorStand.getName().getString().contains(minibossName) && isInQuestTypeWithoutBoss(type)) {
					quest.onMinibossSpawn(armorStand, type);
				}
			}
		}
	}

	/**
	 * Allows updating information about the current Slayer corresponding to the scoreboard.
	 * <p>
	 * Triggering a quest if detection by the chat has not been done.
	 * Assigning the {@link SlayerType} and {@link SlayerTier} of Slayer.
	 * Triggering the Boss Fight if the Boss has spawned “without” the player nearby.
	 *
	 * @param onScoreboardUpdate whether the update is performed periodically or after joining a server
	 * @param scoreboardLines    the lines
	 */
	private void updateSlayerBossInfo(boolean onScoreboardUpdate, List<String> scoreboardLines) {
		if (onScoreboardUpdate && quest == null) return;

		try {
			for (String line : scoreboardLines) {
				Matcher slayerTierMatcher = SLAYER_TIER_PATTERN.matcher(line);
				if (slayerTierMatcher.find()) {
					if (quest == null
							|| !slayerTierMatcher.group(1).equals(quest.getSlayerType().getBossName())
							|| !slayerTierMatcher.group(2).equals(quest.getSlayerTier().name())
					) {
						quest = new SlayerQuest(this);
					}

					SlayerType slayerType = SlayerType.fromBossName(slayerTierMatcher.group(1));
					SlayerTier slayerTier = SlayerTier.valueOf(slayerTierMatcher.group(2));
					quest.setSlayerType(slayerType);
					quest.setSlayerTier(slayerTier);
					SkyBlockEvents.SLAYER_QUEST_START_EVENT.invoker().onStart(slayerType, slayerTier, true);

				} else if (line.equals(SCOREBOARD_BOSS_SPAWNED) && !isBossSpawned()) {
					bossFight = new SlayerBossFight(this, null);
				}
			}
		} catch (Exception ignored) {
		}
	}

	@Nullable
	SlayerQuest getQuest() {
		return quest;
	}

	@Nullable
	<T extends Entity> T findClosestEntity(@Nullable EntityType<T> entityType, @Nullable ArmorStand armorStand) {
		if (entityType == null) return null;
		if (armorStand == null) return null;

		List<T> entities = armorStand.level().getEntities(
				entityType,
				armorStand.getBoundingBox().inflate(0, 1.5D, 0),
				e -> e.isAlive() && !(e instanceof Mob mob && mob.isBaby())
		);

		entities.sort(Comparator.comparingDouble(armorStand::distanceToSqr));

		return switch (entities.size()) {
			case 0 -> null;
			case 1 -> entities.getFirst();
			default -> entities.stream()
					.min(Comparator.comparingInt(entity -> Math.abs(entity.tickCount - armorStand.tickCount)))
					.get();
		};
	}

	private List<Entity> getArmorStands(@NonNull Entity entity) {
		return entity.level().getEntities(
				entity,
				entity.getBoundingBox().inflate(0.1D, 1.5D, 0.1D),
				e -> e instanceof ArmorStand && e.hasCustomName());
	}
}
