package fr.siroz.cariboustonks.features.ui.tracking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.feature.FeatureManager;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.features.fishing.RareSeaCreatureFeature;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.screens.mobtracking.MobTrackingScreen;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.MinecraftUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MobTrackingFeature extends Feature {
	// Après "[LvXXX] ", le 1er char doit être un symbole (non-lettre) qui est le MobType.
	// "[^\p{L}]" -> UN caractère qui N'EST PAS une lettre Unicode (emoji, symbole...)
	private static final Pattern MOB_PATTERN = Pattern.compile("\\[Lv\\d+]\\s+[^\\p{L}]");
	private static final int MAX_TRACKED_ENTITIES = 3;

	private final SlayerManager slayerManager;
	private final MobTrackingRegistry registry;
	private final BossEvent bossEvent;
	private final Cache<Integer, Integer> notified;
	private @Nullable RareSeaCreatureFeature rareSeaCreatureFeature;

	private final List<TrackedEntity> tracked = new ArrayList<>(MAX_TRACKED_ENTITIES);
	private final Map<Integer, Boolean> trackedHighlight = new HashMap<>();
	private boolean showingBossBar = false;

	public MobTrackingFeature() {
		this.slayerManager = CaribouStonks.skyBlock().getSlayerManager();
		this.registry = new MobTrackingRegistry();
		this.bossEvent = new LerpingBossEvent(
				UUID.randomUUID(), Component.empty(), 1f,
				BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS,
				false, false, false
		);
		this.notified = CacheBuilder.newBuilder()
				.expireAfterWrite(10, TimeUnit.SECONDS)
				.build();

		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVE_EVENT.register(this::onRemoveArmorStand);
		ClientEntityEvents.ENTITY_LOAD.register((entity, _) -> this.onEntityLoad(entity));
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, _) -> this.onEntityUnload(entity));

		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.namespaced("mobTracking", ctx -> {
					ctx.executes(ClientContext.openScreen(() -> MobTrackingScreen.create(null)));
				})
				.build());

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("hud_mob_tracking"))
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

		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.of(entity -> {
			if (!(entity instanceof ArmorStand) && trackedHighlight.getOrDefault(entity.getId(), false)) {
				return this.config().uiAndVisuals.mobTracking.highlightColor.getRGB();
			}
			return EntityGlowComponent.EntityGlowStrategy.DEFAULT;
		}));
	}

	public MobTrackingRegistry getRegistry() {
		return registry;
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& SkyBlockAPI.getIsland() != IslandType.KUUDRA_HOLLOW
				&& this.config().uiAndVisuals.mobTracking.tracking;
	}

	@Override
	protected void postInitialize(@NonNull FeatureManager features) {
		rareSeaCreatureFeature = features.getFeature(RareSeaCreatureFeature.class);
	}

	@Override
	protected void onClientJoinServer() {
		tracked.clear();
		trackedHighlight.clear();
		notified.invalidateAll();
		if (showingBossBar) {
			PlayerContext.removeBossBar(bossEvent);
			showingBossBar = false;
		}
	}

	@Override
	protected void onClientTick() {
		if (MINECRAFT.player == null || MINECRAFT.level == null) return;
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
				PlayerContext.showBossBar(bossEvent);
				showingBossBar = true;
			}
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(ArmorStand armorStand, boolean equipment) {
		if (MINECRAFT.player == null || MINECRAFT.level == null) return;
		if (!armorStand.hasCustomName() || armorStand.getCustomName() == null) return;
		if (equipment || !isEnabled()) return;
		if (isAlreadyTracked(armorStand)) return;

		try {
			String armorStandName = armorStand.getCustomName().getString();
			// MobTracking Patch - Permet d'éviter de détecter les ArmorStand des Pets ou autre
			if (!MOB_PATTERN.matcher(armorStandName).find()) return;

			// La recherche se fait uniquement si (dans l'ordre).
			// - La config est activé.
			// - L'Island actuel est présent dans sa liste.
			// Le nom est présent.
			MobTrackingRegistry.MobTrackingEntry mobEntry = registry.findMob(
					armorStandName,
					MobTrackingRegistry.CONTAINS,
					SkyBlockAPI.getIsland()
			);
			if (mobEntry != null) {
				addTrackedEntity(new TrackedEntity(armorStand, mobEntry.priority()));
				notifyEntity(armorStand, mobEntry);
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
				PlayerContext.removeBossBar(bossEvent);
			}
		}
	}

	@EventHandler(event = "ClientEntityEvents.ENTITY_LOAD")
	private void onEntityLoad(Entity entity) {
		if (!isEnabled()) return;
		if (entity instanceof ArmorStand) return;
		// MobTracking Patch - Évite les joueurs avec un nom de mobs -_-
		if (MinecraftUtils.isPlayer(entity)) return;

		MobTrackingRegistry.MobTrackingEntry mobEntry = registry.findMob(
				entity.getName().getString(),
				MobTrackingRegistry.EQUALS,
				SkyBlockAPI.getIsland()
		);
		if (mobEntry != null) {
			trackedHighlight.put(entity.getId(), mobEntry.model().isHighlightable());
			notifyEntity(entity, mobEntry);
		}
	}

	@EventHandler(event = "ClientEntityEvents.ENTITY_UNLOAD")
	private void onEntityUnload(Entity entity) {
		trackedHighlight.remove(entity.getId());
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

	private void notifyEntity(@NonNull Entity entity, MobTrackingRegistry.@NonNull MobTrackingEntry mobEntry) {
		int entityId = entity.getId();

		if (notified.getIfPresent(entityId) == null && mobEntry.model().isNotifyOnSpawn()) {
			notified.put(entityId, entityId);

			if (mobEntry.category() == MobTrackingRegistry.MobCategory.FISHING
					&& rareSeaCreatureFeature != null && rareSeaCreatureFeature.hasFoundCreature()
			) {
				// Évite de trigger le Title/Subtiltle si le joueur a une RareSeaCreature a lui,
				// pour garder la notification du côté de RareSeaCreatureFeature
				return;
			}

			// TODO - Avoir dans le registry des Predicate prédéfini pour certains mobs
			//  sous forme de class pré-faite et qui peuvent être utiliser au moment du register
			//  > OneNotificationTrackingPredicate
			//  > PositionTrackingPredicate
			//  > ..
			if (entity.position().y() >= 74 && mobEntry.model().getName().equals("Puddle Jumper")) {
				// SIROZ-NOTE: en attendant je block le Jumper car c casse pied la notif a chaque fois qu'il jump
				return;
			}

			PlayerContext.showTitleAndSubtitle(
					mobEntry.displayName(),
					Component.literal(this.config().uiAndVisuals.mobTracking.spawnMessage),
					1, 25, 1
			);
			if (this.config().uiAndVisuals.mobTracking.playSoundWhenSpawn) {
				PlayerContext.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
			}
		}
	}

	private void getHudLines(HudElementBuilder builder) {
		if (tracked.isEmpty()) return;

		// SIROZ-NOTE: mettre une option si on affiche tout, 1 seul ou de facon custom a l'avenir
		for (TrackedEntity entity : tracked) {
			Component customName = entity.armorStand().getCustomName();
			if (customName != null) {
				builder.appendLine(customName);
			}
		}
	}
}
