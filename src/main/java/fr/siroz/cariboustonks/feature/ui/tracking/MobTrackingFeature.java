package fr.siroz.cariboustonks.feature.ui.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.screen.mobtracking.MobTrackingScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public class MobTrackingFeature extends Feature /*implements HudProvider*/ {

	private static final int MAX_TRACKED_ENTITIES = 3;

	private final SlayerManager slayerManager;
	private final MobTrackingRegistry registry;
	private final BossEvent bossEvent;

	private final List<TrackedEntity> tracked = new ArrayList<>(MAX_TRACKED_ENTITIES);

	public MobTrackingFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);
		this.registry = new MobTrackingRegistry();
		this.bossEvent = new LerpingBossEvent(
				UUID.randomUUID(), Component.empty(), 1f,
				BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS,
				false, false, false
		);

		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("mobTracking")
						.executes(Client.openScreen(() -> MobTrackingScreen.create(null))))
		));
	}

	@ApiStatus.Internal
	public MobTrackingRegistry getRegistry() {
		return registry;
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& SkyBlockAPI.getIsland() != IslandType.KUUDRA_HOLLOW
				&& ConfigManager.getConfig().uiAndVisuals.mobTracking.enabled;
	}

	@Override
	protected void onClientJoinServer() {
		tracked.clear();
	}

	@Override
	protected void onClientTick() {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!isEnabled()) return;

		if (slayerManager.isInQuest()) {
			ArmorStand slayerBoss = slayerManager.getBossArmorStand();
			if (slayerBoss != null && ConfigManager.getConfig().uiAndVisuals.mobTracking.enableSlayer) {
				updateSlayerBoss(slayerBoss);
			}
		}

		// Nettoyage des entités invalides, pour les cocoons de temps en temps,
		// ils se font remove ici, donc par précaution.
		tracked.removeIf(entity -> !entity.isValid());

		if (tracked.isEmpty()) {
			return;
		}

		TrackedEntity topPriority = tracked.getFirst();
		Component name = topPriority.armorStand().getCustomName();
		if (name != null) {
			bossEvent.setName(name);
			Client.showBossBar(bossEvent);
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NotNull ArmorStand armorStand, boolean equipment) {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!armorStand.hasCustomName() || armorStand.getCustomName() == null) return;
		if (equipment || !isEnabled()) return;
		if (isAlreadyTracked(armorStand)) return;

		try {
			// La recherche se fait uniquement si (dans l'ordre).
			// - La config est activé.
			// - L'Island actuel est présent dans sa liste.
			// Le nom est présent.
			MobTrackingRegistry.MobTrackingEntry mobEntry = registry.findMob(
					armorStand.getCustomName().getString(),
					SkyBlockAPI.getIsland()
			);
			if (mobEntry != null) {
				addTrackedEntity(new TrackedEntity(armorStand, mobEntry.priority()));
				onTrackEntity(mobEntry);
			}
		} catch (Exception ex) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.warn("{} Unable to find tracked entity", getShortName(), ex);
			}
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVED")
	private void onRemoveArmorStand(@NotNull ArmorStand armorStand) {
		if (!tracked.isEmpty()) {
			boolean removed = tracked.removeIf(entity -> entity.armorStand().getId() == armorStand.getId());
			if (removed) {
				Client.removeBossBar(bossEvent);
			}
		}
	}

	private void updateSlayerBoss(@NotNull ArmorStand slayerBoss) {
		if (slayerBoss.getCustomName() == null) return;

		// Retirer l'ancien slayer boss si présent et le (re)ajouter en first
		// Si jamais le "onUpdateArmorStand" le récupère en premier.
		tracked.removeIf(TrackedEntity::isSlayerBoss);
		tracked.addFirst(new TrackedEntity(slayerBoss, 100, true));

		while (tracked.size() > MAX_TRACKED_ENTITIES) {
			tracked.removeLast();
		}
	}

	private boolean isAlreadyTracked(@NotNull ArmorStand armorStand) {
		int id = armorStand.getId();
		for (TrackedEntity entity : tracked) {
			if (entity.armorStand().getId() == id) {
				return true;
			}
		}
		return false;
	}

	private void addTrackedEntity(@NotNull TrackedEntity entity) {
		tracked.add(entity);

		Collections.sort(tracked);

		while (tracked.size() > MAX_TRACKED_ENTITIES) {
			tracked.removeLast();
		}
	}

	private void onTrackEntity(@NotNull MobTrackingRegistry.MobTrackingEntry mobEntry) {
		if (mobEntry.config().notifyOnSpawn) {
			Client.showTitleAndSubtitle(
					mobEntry.displayName(),
					Component.literal("Nearby!").withColor(Colors.AQUA.asInt()),
					1, 20, 1
			);
			if (ConfigManager.getConfig().uiAndVisuals.mobTracking.playSoundWhenSpawn) {
				Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
			}
		}
	}
}
