package fr.siroz.cariboustonks.features.ui.deployable;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.HeadTextures;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class DeployableFeature extends Feature {
	private static final Pattern DEPLOYABLE_PATTERN = Pattern.compile("^(.+?)\\s+(\\d+)s$");
	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_deployable");

	private final HudElementBuilder hudBuilder;
	private final List<TrackedDeployable> trackedDeployables = new CopyOnWriteArrayList<>(); // SIROZ-NOTE :: non-concurrent

	public DeployableFeature() {
		this.hudBuilder = new HudElementBuilder();

		TickScheduler.getInstance().runRepeating(this::updateDeployables, 1, TimeUnit.SECONDS);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
		WorldEvents.ARMORSTAND_REMOVE_EVENT.register(this::onRemoveArmorStand);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new MultiElementHud(
						() -> this.isEnabled() && !this.trackedDeployables.isEmpty() && this.config().uiAndVisuals.deployables.hud.showHud,
						new HudElementTextBuilder()
								.append(Component.literal("§d§lPlasmaflux §e15s"))
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
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.deployables.enabled;
	}

	@Override
	protected void onClientJoinServer() {
		trackedDeployables.clear();
	}

	private void updateDeployables() {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (trackedDeployables.isEmpty()) return;
		if (!isEnabled()) return;

		for (TrackedDeployable trackedDeployable : trackedDeployables) {
			ArmorStand armorStand = trackedDeployable.getArmorStand();
			if (armorStand == null) continue;

			Vec3 playerPos = CLIENT.player.position();
			double deployableRange = trackedDeployable.getDeployable().getBlockRange();
			double deployableDistanceSQ = deployableRange * deployableRange;
			trackedDeployable.setActive(!(armorStand.position().distanceToSqr(playerPos) > deployableDistanceSQ));
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NonNull ArmorStand armorStand, boolean equipment) {
		if (CLIENT.player == null || CLIENT.level == null) return;
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
	private void onRemoveArmorStand(ArmorStand armorStand) {
		if (!trackedDeployables.isEmpty()) {
			trackedDeployables.removeIf(entity -> entity.getArmorStand().getId() == armorStand.getId());
		}
	}

	private void detectPowerOrbs(@NonNull ArmorStand armorStand) {
		if (!this.config().uiAndVisuals.deployables.detectPowerOrbs) return;
		if (armorStand.getCustomName() == null) return;
		if (!armorStand.hasCustomName()) return;

		String customName = armorStand.getCustomName().getString();
		if (customName.isBlank()) return;

		tryDeployableWithTimer(armorStand, type -> type != Deployable.Type.FLARE && type != Deployable.Type.PERSONAL);
	}

	private void detectFlares(@NonNull ArmorStand armorStand) {
		if (!this.config().uiAndVisuals.deployables.detectFlares) return;
		if (armorStand.hasCustomName() || armorStand.isCustomNameVisible()) return;
		if (!armorStand.hasItemInSlot(EquipmentSlot.HEAD)) return;

		// SIROZ-NOTE: Experimental, le check est trop lourd puis

		String headTexture = ItemUtils.getHeadTexture(armorStand.getItemBySlot(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) return;

		// SIROZ-NOTE: revoir le check entre les Flares qui n'est pas propre
		if (headTexture.equals(HeadTextures.SOS_FLARE)) {
			addDeployable(new TrackedDeployable(armorStand, Deployable.SOS_FLARE));

		} else if (headTexture.equals(HeadTextures.ALERT_FLARE)) {
			addDeployable(new TrackedDeployable(armorStand, Deployable.ALERT_FLARE));
		}
	}

	private void detectPersonals(@NonNull ArmorStand armorStand) {
		if (!this.config().uiAndVisuals.deployables.detectPersonals) return;
		if (armorStand.getCustomName() == null) return;
		if (!armorStand.hasCustomName()) return;

		String customName = armorStand.getCustomName().getString();
		if (customName.isBlank()) return;

		if (customName.contains(CLIENT.getUser().getName())) {
			List<Entity> entities = armorStand.level().getEntities(armorStand,
					armorStand.getBoundingBox().inflate(0.1D, 1.5D, 0.1D),
					target -> target instanceof ArmorStand && target.getCustomName() != null
			);
			for (Entity other : entities) {
				ArmorStand otherArmorStand = (ArmorStand) other;
				if (tryDeployableWithTimer(otherArmorStand, type -> type == Deployable.Type.PERSONAL)) {
					break;
				}
			}
		}
	}

	private boolean tryDeployableWithTimer(
			@NonNull ArmorStand armorStand,
			@NonNull Predicate<Deployable.Type> predicate
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

	private void addDeployable(@NonNull TrackedDeployable trackedDeployable) {
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

	private boolean isAlreadyTracked(@NonNull ArmorStand armorStand) {
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
			Component displayName = deployable.getDisplayName();
			Component customName = tracked.getArmorStand().getCustomName();

			if (displayName != null && deployable.getType() == Deployable.Type.FLARE) {
				hudBuilder.appendIconLine(deployable.getItemDisplay(), displayName);

			} else if (customName != null) {
				hudBuilder.appendIconLine(deployable.getItemDisplay(), customName);
			}
		}

		return hudBuilder.build();
	}
}
