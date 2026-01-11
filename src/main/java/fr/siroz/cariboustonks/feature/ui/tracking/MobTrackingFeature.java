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
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.MultiElementHud;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.screen.mobtracking.MobTrackingScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public class MobTrackingFeature extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_mob_tracking");
	private static final int MAX_TRACKED_ENTITIES = 3;

	private final SlayerManager slayerManager;
	private final MobTrackingRegistry registry;
	private final BossBar bossEvent;
	private final HudElementBuilder hudBuilder;

	private final List<TrackedEntity> tracked = new ArrayList<>(MAX_TRACKED_ENTITIES);
	private boolean showingBossBar = false;

	public MobTrackingFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);
		this.registry = new MobTrackingRegistry();
		this.bossEvent = new ClientBossBar(
				UUID.randomUUID(), Text.empty(), 1f,
				BossBar.Color.RED, BossBar.Style.PROGRESS,
				false, false, false
		);
		this.hudBuilder = new HudElementBuilder();

		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("mobTracking")
						.executes(StonksUtils.openScreen(() -> MobTrackingScreen.create(null))))
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
		if (showingBossBar) {
			Client.removeBossBar(bossEvent);
			showingBossBar = false;
		}
	}

	@Override
	protected void onClientTick() {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled()) return;

		if (slayerManager.isInQuest()) {
			ArmorStandEntity slayerBoss = slayerManager.getBossArmorStand();
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

		if (ConfigManager.getConfig().uiAndVisuals.mobTracking.showInBossBar) {
			Text name = tracked.getFirst().armorStand().getCustomName();
			if (name != null) {
				bossEvent.setName(name);
				Client.showBossBar(bossEvent);
				showingBossBar = true;
			}
		}
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new MultiElementHud(
				() -> this.isEnabled() && !tracked.isEmpty() && ConfigManager.getConfig().uiAndVisuals.mobTracking.hud.showInHud,
				new HudElementTextBuilder()
						.append(Text.literal("§8[§7Lv750§8] §2✿§e✰§d❃ §2Exalted Minos Inquisitor §a45.8M§f/§a50M§c❤"))
						.append(Text.literal("§e﴾ §8[§7Lv200§8] §8☠§f\uD83E\uDDB4§5♃ §8§lBladesoul§r §a50M§f/§a50M§c❤ §e﴿"))
						.build(),
				this::getHudLines,
				ConfigManager.getConfig().uiAndVisuals.mobTracking.hud,
				125,
				25
		);
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NotNull ArmorStandEntity armorStand, boolean equipment) {
		if (CLIENT.player == null || CLIENT.world == null) return;
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
	private void onRemoveArmorStand(@NotNull ArmorStandEntity armorStand) {
		if (!tracked.isEmpty()) {
			boolean removed = tracked.removeIf(entity -> entity.armorStand().getId() == armorStand.getId());
			if (removed) {
				Client.removeBossBar(bossEvent);
			}
		}
	}

	private void updateSlayerBoss(@NotNull ArmorStandEntity slayerBoss) {
		if (slayerBoss.getCustomName() == null) return;

		// Retirer l'ancien slayer boss si présent et le (re)ajouter en first
		// Si jamais le "onUpdateArmorStand" le récupère en premier.
		tracked.removeIf(TrackedEntity::isSlayerBoss);
		tracked.addFirst(new TrackedEntity(slayerBoss, 100, true));

		while (tracked.size() > MAX_TRACKED_ENTITIES) {
			tracked.removeLast();
		}
	}

	private boolean isAlreadyTracked(@NotNull ArmorStandEntity armorStand) {
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
					Text.literal(ConfigManager.getConfig().uiAndVisuals.mobTracking.spawnMessage),
					1, 20, 1
			);
			if (ConfigManager.getConfig().uiAndVisuals.mobTracking.playSoundWhenSpawn) {
				Client.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1f, 1f);
			}
		}
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		// SIROZ-NOTE: mettre une option si on affiche tout, 1 seul ou de facon custom a l'avenir
		for (TrackedEntity entity : tracked) {
			Text customName = entity.armorStand().getCustomName();
			if (customName != null) {
				hudBuilder.appendLine(customName);
			}
		}

		return hudBuilder.build();
	}
}
