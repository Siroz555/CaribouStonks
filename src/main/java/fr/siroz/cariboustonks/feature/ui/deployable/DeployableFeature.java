package fr.siroz.cariboustonks.feature.ui.deployable;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.MultiElementHud;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import it.unimi.dsi.fastutil.Pair;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class DeployableFeature extends Feature implements HudProvider {
	private static final Pattern DEPLOYABLE_PATTERN = Pattern.compile("^(.+?)\\s+(\\d+)s$");
	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_deployable");

	private final HudElementBuilder hudBuilder;
	private final List<TrackedDeployable> trackedDeployables = new CopyOnWriteArrayList<>(); // SIROZ-NOTE :: non-concurrent

	public DeployableFeature() {
		this.hudBuilder = new HudElementBuilder();

		TickScheduler.getInstance().runRepeating(this::updateDeployables, 1, TimeUnit.SECONDS);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.deployables.enabled;
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new MultiElementHud(
				() -> this.isEnabled() && !this.trackedDeployables.isEmpty() && ConfigManager.getConfig().uiAndVisuals.deployables.hud.showHud,
				new HudElementTextBuilder()
						.append(Text.literal("§d§lPlasmaflux §e15s"))
						.build(),
				this::getHudLines,
				ConfigManager.getConfig().uiAndVisuals.deployables.hud,
				150,
				15
		);
	}

	@Override
	protected void onClientJoinServer() {
		trackedDeployables.clear();
	}

	private void updateDeployables() {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (trackedDeployables.isEmpty()) return;
		if (!isEnabled()) return;

		for (TrackedDeployable trackedDeployable : trackedDeployables) {
			ArmorStandEntity armorStand = trackedDeployable.getArmorStand();
			if (armorStand == null) continue;

			Vec3d playerPos = CLIENT.player.getEntityPos();
			double deployableRange = trackedDeployable.getDeployable().getBlockRange();
			double deployableDistanceSQ = deployableRange * deployableRange;
			trackedDeployable.setActive(!(armorStand.getEntityPos().squaredDistanceTo(playerPos) > deployableDistanceSQ));
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(ArmorStandEntity armorStand, boolean equipment) {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled()) return;
		if (isAlreadyTracked(armorStand)) return;

		if (equipment) {
			// Contrairement aux Power Orbs, les flares passe par l'update de l'équipement, pourquoi ? jcp
			// Pas de nom, check avec la texture -_-
			detectFlares(armorStand);
		} else {
			// Power Orbs > Plasmaflux, Umberella, ... "Will-o'-wisp 34s".
			detectPowerOrbs(armorStand);
			// Personal > Black Hole, Totem, ... "Black Hole 34s". (avec le nom du joueur en dessous, détection multiple)
			detectPersonals(armorStand);
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVE_EVENT")
	private void onRemoveArmorStand(ArmorStandEntity armorStand) {
		if (!trackedDeployables.isEmpty()) {
			trackedDeployables.removeIf(entity -> entity.getArmorStand().getId() == armorStand.getId());
		}
	}

	private void detectPowerOrbs(ArmorStandEntity armorStand) {
		if (!ConfigManager.getConfig().uiAndVisuals.deployables.detectPowerOrbs) return;
		if (armorStand.getCustomName() == null) return;
		if (!armorStand.hasCustomName()) return;

		String customName = armorStand.getCustomName().getString();
		if (customName.isBlank()) return;

		tryDeployableWithTimer(armorStand, type -> type == Deployable.Type.COMBAT || type == Deployable.Type.PERSONAL || type == Deployable.Type.FISHING);
	}

	private void detectFlares(ArmorStandEntity armorStand) {
		if (!ConfigManager.getConfig().uiAndVisuals.deployables.detectFlares) return;
		if (armorStand.hasCustomName() || armorStand.isCustomNameVisible()) return;
		if (!armorStand.hasStackEquipped(EquipmentSlot.HEAD)) return;

		// SIROZ-NOTE: Experimental, le check est trop lourd puis

		String headTexture = ItemUtils.getHeadTexture(armorStand.getEquippedStack(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) return;

		// SIROZ-NOTE: revoir le check entre les Flares qui n'est pas propre
		if (headTexture.equals(HeadTextures.SOS_FLARE)) {
			addDeployable(new TrackedDeployable(armorStand, Deployable.SOS_FLARE));

		} else if (headTexture.equals(HeadTextures.ALERT_FLARE)) {
			addDeployable(new TrackedDeployable(armorStand, Deployable.ALERT_FLARE));
		}
	}

	private void detectPersonals(ArmorStandEntity armorStand) {
		if (!ConfigManager.getConfig().uiAndVisuals.deployables.detectPersonals) return;
		if (armorStand.getCustomName() == null) return;
		if (!armorStand.hasCustomName()) return;

		String customName = armorStand.getCustomName().getString();
		if (customName.isBlank()) return;

		if (customName.contains(CLIENT.getSession().getUsername())) {
			List<Entity> entities = armorStand.getEntityWorld().getOtherEntities(armorStand,
					armorStand.getBoundingBox().expand(0.1D, 1.5D, 0.1D),
					target -> target instanceof ArmorStandEntity && target.getCustomName() != null
			);
			for (Entity other : entities) {
				ArmorStandEntity otherArmorStand = (ArmorStandEntity) other;
				if (tryDeployableWithTimer(otherArmorStand, type -> type == Deployable.Type.PERSONAL)) {
					break;
				}
			}
		}
	}

	private boolean tryDeployableWithTimer(
			ArmorStandEntity armorStand,
			Predicate<Deployable.Type> predicate
	) {
		if (armorStand.getCustomName() == null) return false;

		try {
			String otherName = armorStand.getCustomName().getString();
			Matcher matcher = DEPLOYABLE_PATTERN.matcher(otherName);
			if (!matcher.matches()) return false;

			String name = matcher.group(1);
			int seconds = Integer.parseInt(matcher.group(2));
			if (name == null || name.isBlank() || seconds <= 0) return false;

			for (Deployable deployable : Deployable.VALUES) {
				if (!predicate.test(deployable.getType())) continue;

				if (name.equals(deployable.getName())) {
					addDeployable(new TrackedDeployable(armorStand, deployable));
					return true;
				}
			}
		} catch (Exception ex) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.warn("{} Failed to parse armorStand", getShortName(), ex);
			}
		}
		return false;
	}

	private void addDeployable(TrackedDeployable trackedDeployable) {
		Deployable newDeployable = trackedDeployable.getDeployable();
		Deployable.Type newType = newDeployable.getType();
		int newPriority = newDeployable.getPriority();
		// Même Deployable, le plus récent remplace toujours
		trackedDeployables.removeIf(d -> d.getDeployable().ordinal() == newDeployable.ordinal());
		// Une seule entrée par Type, la plus prioritaire est keep
		TrackedDeployable sameType = trackedDeployables.stream()
				.filter(d -> d.getDeployable().getType() == newType)
				.findFirst()
				.orElse(null);

		if (sameType != null) {
			if (newPriority > sameType.getDeployable().getPriority()) {
				// Nouveau plus prioritaire, remplace l'existant
				trackedDeployables.remove(sameType);
			} else {
				// Priorité insuffisante
				return;
			}
		}

		trackedDeployables.add(trackedDeployable);
		Collections.sort(trackedDeployables);
	}

	private boolean isAlreadyTracked(ArmorStandEntity armorStand) {
		int id = armorStand.getId();
		for (TrackedDeployable entity : trackedDeployables) {
			if (entity.getArmorStand().getId() == id) {
				return true;
			}
		}
		return false;
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		for (TrackedDeployable tracked : trackedDeployables) {
			if (!tracked.isActive()) continue;

			Deployable deployable = tracked.getDeployable();
			Text displayName = deployable.getDisplayName();
			Text customName = tracked.getArmorStand().getCustomName();

			if (displayName != null && deployable.getType() == Deployable.Type.FLARE) {
				hudBuilder.appendIconLine(deployable.getItemDisplay(), displayName);

			} else if (customName != null) {
				hudBuilder.appendIconLine(deployable.getItemDisplay(), customName);
			}
		}

		return hudBuilder.build();
	}
}
