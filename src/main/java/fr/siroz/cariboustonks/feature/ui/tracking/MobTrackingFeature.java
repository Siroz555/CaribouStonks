package fr.siroz.cariboustonks.feature.ui.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.screen.mobtracking.MobTrackingScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;

@Experimental
public class MobTrackingFeature extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_mob_tracking");
	private static final int MAX_TRACKED_ENTITIES = 3;

	private final SlayerManager slayerManager;
	private final MobTrackingRegistry registry;
	private final BossEvent bossEvent;
	private final HudElementBuilder hudBuilder;

	private final List<TrackedEntity> tracked = new ArrayList<>(MAX_TRACKED_ENTITIES);
	private boolean showingBossBar = false;

	public MobTrackingFeature() {
		this.slayerManager = CaribouStonks.skyBlock().getSlayerManager();
		this.registry = new MobTrackingRegistry();
		this.bossEvent = new LerpingBossEvent(
				UUID.randomUUID(), Component.empty(), 1f,
				BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS,
				false, false, false
		);
		this.hudBuilder = new HudElementBuilder();

		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVE_EVENT.register(this::onRemoveArmorStand);

		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.namespaced("mobTracking", ctx -> {
					ctx.executes(Client.openScreen(() -> MobTrackingScreen.create(null)));
				})
				.build());

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new MultiElementHud(
						() -> this.isEnabled() && !tracked.isEmpty() && this.config().uiAndVisuals.mobTracking.hud.showInHud,
						new HudElementTextBuilder()
								.append(Component.literal("§8[§7Lv750§8] §2✿§e✰§d❃ §2Exalted Minos Inquisitor §a45.8M§f/§a50M§c❤"))
								.append(Component.literal("§e﴾ §8[§7Lv200§8] §8☠§f\uD83E\uDDB4§5♃ §8§lBladesoul§r §a50M§f/§a50M§c❤ §e﴿"))
								.build(),
						this::getHudLines,
						this.config().uiAndVisuals.mobTracking.hud,
						125,
						25
				))
				.build());
	}

	public MobTrackingRegistry getRegistry() {
		return registry;
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& SkyBlockAPI.getIsland() != IslandType.KUUDRA_HOLLOW
				&& this.config().uiAndVisuals.mobTracking.enabled;
	}

	@Override
	protected void onClientJoinServer() {
		tracked.clear();
		if (showingBossBar) {
			Client.removeBossBar(bossEvent);
			showingBossBar = false;
		}
	}

	@Override
	protected void onClientTick() {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!isEnabled()) return;

		if (slayerManager.isInQuest()) {
			ArmorStand slayerBoss = slayerManager.getBossArmorStand();
			if (slayerBoss != null && this.config().uiAndVisuals.mobTracking.enableSlayer) {
				updateSlayerBoss(slayerBoss);
			}
		}

		// Nettoyage des entités invalides, pour les cocoons de temps en temps,
		// ils se font remove ici, donc par précaution.
		tracked.removeIf(entity -> !entity.isValid());

		if (tracked.isEmpty()) {
			return;
		}

		if (this.config().uiAndVisuals.mobTracking.showInBossBar) {
			Component name = tracked.getFirst().armorStand().getCustomName();
			if (name != null) {
				bossEvent.setName(name);
				Client.showBossBar(bossEvent);
				showingBossBar = true;
			}
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(ArmorStand armorStand, boolean equipment) {
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

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVE_EVENT")
	private void onRemoveArmorStand(ArmorStand armorStand) {
		if (!tracked.isEmpty()) {
			boolean removed = tracked.removeIf(entity -> entity.armorStand().getId() == armorStand.getId());
			if (removed) {
				Client.removeBossBar(bossEvent);
			}
		}
	}

	private void updateSlayerBoss(@NonNull ArmorStand slayerBoss) {
		if (slayerBoss.getCustomName() == null) return;

		// Retirer l'ancien slayer boss si présent et le (re)ajouter en first
		// Si jamais le "onUpdateArmorStand" le récupère en premier.
		tracked.removeIf(TrackedEntity::isSlayerBoss);
		tracked.addFirst(new TrackedEntity(slayerBoss, 100, true));

		while (tracked.size() > MAX_TRACKED_ENTITIES) {
			tracked.removeLast();
		}
	}

	private boolean isAlreadyTracked(@NonNull ArmorStand armorStand) {
		int id = armorStand.getId();
		for (TrackedEntity entity : tracked) {
			if (entity.armorStand().getId() == id) {
				return true;
			}
		}
		return false;
	}

	private void addTrackedEntity(TrackedEntity entity) {
		tracked.add(entity);

		Collections.sort(tracked);

		while (tracked.size() > MAX_TRACKED_ENTITIES) {
			tracked.removeLast();
		}
	}

	private void onTrackEntity(MobTrackingRegistry.@NonNull MobTrackingEntry mobEntry) {
		if (mobEntry.model().isNotifyOnSpawn()) {
			Client.showTitleAndSubtitle(
					mobEntry.displayName(),
					Component.literal(this.config().uiAndVisuals.mobTracking.spawnMessage),
					1, 20, 1
			);
			if (this.config().uiAndVisuals.mobTracking.playSoundWhenSpawn) {
				Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
			}
		}
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		// SIROZ-NOTE: mettre une option si on affiche tout, 1 seul ou de facon custom a l'avenir
		for (TrackedEntity entity : tracked) {
			Component customName = entity.armorStand().getCustomName();
			if (customName != null) {
				hudBuilder.appendLine(customName);
			}
		}

		return hudBuilder.build();
	}
}
