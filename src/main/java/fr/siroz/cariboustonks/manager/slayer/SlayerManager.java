package fr.siroz.cariboustonks.manager.slayer;

import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.HudEvents;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.util.ScoreboardUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Master Manager regarding Slayers.
 * <p>
 * Allows detecting, manage, and call various events related to Slayer Quests.
 *
 * @see SlayerType
 * @see SlayerTier
 * @see SkyBlockEvents#SLAYER_BOSS_SPAWN
 * @see SkyBlockEvents#SLAYER_BOSS_DEATH
 * @see SkyBlockEvents#SLAYER_MINIBOSS_SPAWN
 * @see SkyBlockEvents#SLAYER_QUEST_START
 * @see SkyBlockEvents#SLAYER_QUEST_FAIL
 */
public final class SlayerManager implements Manager {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

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
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
		SkyBlockEvents.ISLAND_CHANGE.register(this::onIslandChange);
		HudEvents.SCOREBOARD_UPDATE.register(this::onScoreboardUpdate);
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
	public boolean isSlayerTier(@NotNull SlayerTier tier) {
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
	public boolean isInQuestTypeWithoutBoss(@NotNull SlayerType slayerType) {
		return !isBossSpawned() && quest != null && quest.getSlayerType().equals(slayerType);
	}

	/**
	 * Returns a List of {@link Entity} representing the Minibosses during the player's quest.
	 *
	 * @return the entity Minibosses list or an empty list
	 */
	@NotNull
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
	 * The main entry point for managing the various statuses of the Slayer Quest's progress.
	 */
	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(@NotNull Text text) {
		if (!SkyBlockAPI.isOnSkyBlock()) return;

		String message = text.getString();
		message = message.replaceFirst("^\\s+", "");

		switch (message) {
			case QUEST_CANCELLED, QUEST_FAILED -> {
				if (quest != null) {
					SkyBlockEvents.SLAYER_QUEST_FAIL.invoker().onFail(quest.getSlayerType(), quest.getSlayerTier());
				}
				quest = null;
				bossFight = null;
			}
			case QUEST_STARTED -> {
				if (quest == null) {
					quest = new SlayerQuest(this);
				}
				SkyBlockEvents.SLAYER_QUEST_START.invoker()
						.onStart(quest.getSlayerType(), quest.getSlayerTier(), false);
				bossFight = null;
			}
			case BOSS_SLAIN -> {
				if (quest != null && bossFight != null) {
					bossFight.setSlain(true);
					SkyBlockEvents.SLAYER_BOSS_DEATH.invoker()
							.onDeath(quest.getSlayerType(), quest.getSlayerTier(), bossFight.getBossSpawnTime());
				}
			}
			case QUEST_COMPLETE -> {
				if (quest != null && bossFight != null && !bossFight.isSlain()) {
					SkyBlockEvents.SLAYER_BOSS_DEATH.invoker()
							.onDeath(quest.getSlayerType(), quest.getSlayerTier(), bossFight.getBossSpawnTime());
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
	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE")
	private void onIslandChange(@NotNull IslandType islandType) {
		bossFight = null;
		quest = null;
		TickScheduler.getInstance().runLater(
				() -> updateSlayerBossInfo(false, ScoreboardUtils.getStringScoreboard()),
				3, TimeUnit.SECONDS
		);
	}

	/**
	 * Retrieves the different lines of the scoreboard every second
	 */
	@EventHandler(event = "HudEvents.SCOREBOARD_UPDATE")
	private void onScoreboardUpdate(@NotNull List<String> lines) {
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
	private void onArmorStandUpdate(@NotNull ArmorStandEntity armorStand, boolean equipment) {
		if (!SkyBlockAPI.isOnSkyBlock() || equipment) return;
		if (quest == null || !armorStand.hasCustomName() || (isBossSpawned() && bossFight.getBossEntity() != null)) return;

		if (armorStand.getName().getString().contains(CLIENT.getSession().getUsername())) {
			for (Entity otherArmorStands : getArmorStands(armorStand)) {
				Matcher slayerMatcher = SLAYER_PATTERN.matcher(otherArmorStands.getName().getString());
				if (slayerMatcher.find()) {
					if (bossFight != null && bossFight.getBossEntity() == null) {
						bossFight.tryToFindBoss((ArmorStandEntity) otherArmorStands);
						return;
					}

					bossFight = new SlayerBossFight(this, (ArmorStandEntity) otherArmorStands);
					return;
				}
			}
		}

		if (!armorStand.isInRange(CLIENT.player, 20)) {
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
					SkyBlockEvents.SLAYER_QUEST_START.invoker().onStart(slayerType, slayerTier, true);

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
	<T extends Entity> T findClosestEntity(@Nullable EntityType<T> entityType, @Nullable ArmorStandEntity armorStand) {
		if (entityType == null) return null;
		if (armorStand == null) return null;

		List<T> entities = armorStand.getWorld().getEntitiesByType(
				entityType,
				armorStand.getBoundingBox().expand(0, 1.5D, 0),
				e -> e.isAlive() && !(e instanceof MobEntity mob && mob.isBaby())
		);

		entities.sort(Comparator.comparingDouble(armorStand::squaredDistanceTo));

		return switch (entities.size()) {
			case 0 -> null;
			case 1 -> entities.getFirst();
			default -> entities.stream()
					.min(Comparator.comparingInt(entity -> Math.abs(entity.age - armorStand.age)))
					.get();
		};
	}

	private List<Entity> getArmorStands(@NotNull Entity entity) {
		return entity.getWorld().getOtherEntities(
				entity,
				entity.getBoundingBox().expand(0.1D, 1.5D, 0.1D),
				e -> e instanceof ArmorStandEntity && e.hasCustomName());
	}
}
