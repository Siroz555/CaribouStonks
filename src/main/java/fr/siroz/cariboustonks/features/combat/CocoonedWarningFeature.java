package fr.siroz.cariboustonks.features.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigValue;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.feature.FeatureManager;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.HeadTextures;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerType;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.features.slayer.SlayerCocoonedWarningFeature;
import fr.siroz.cariboustonks.platform.api.render.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.Ticks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CocoonedWarningFeature extends Feature {

	private static final long WORLD_CHANGE_THRESHOLD_MS = 10_000;
	private static final double MAX_ARMORSTAND_PAIR_DISTANCE_SQ = 2f * 2f;
	private static final double MAX_PLAYER_COCOON_DISTANCE_SQ = 15f * 15f;
	private static final long COCOON_LIFE_TIME_TICKS = Ticks.from(6500, TimeUnit.MILLISECONDS); // 6.5s
	//private static final int MAX_Y_CHECK = 6;

	private final ConfigValue<String> configMessage = ConfigValue.of(
			() -> this.config().combat.cocoonedMob.message
	);

	// chaîne temporaire des apparitions successives (ordre d'apparition)
	private final Deque<ArmorStand> chain = new ArrayDeque<>();
	private final List<BlockPos> cocoonPositions = new ArrayList<>();
	// Pas de Map<Cocoon> pour éviter la concurrence entre tick/render et de possible null
	private final Map<BlockPos, Long> cocoonLifeTicks = new ConcurrentHashMap<>();

	private @Nullable SlayerCocoonedWarningFeature slayerCocoonedWarningFeature;
	private long lastWorldChange = 0;
	private boolean canBeTriggered = false;

	public CocoonedWarningFeature() {
		SkyBlockEvents.ISLAND_CHANGE_EVENT.register(this::onChangeIsland);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVE_EVENT.register(this::onRemoveArmorStand);
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& canBeTriggered
				&& !CaribouStonks.skyBlock().getSlayerManager().isInQuestWithBoss(SlayerType.SPIDER)
				&& this.config().combat.cocoonedMob.warning;
	}

	@Override
	protected void postInitialize(@NonNull FeatureManager features) {
		slayerCocoonedWarningFeature = features.getFeature(SlayerCocoonedWarningFeature.class);
	}

	@Override
	protected void onClientJoinServer() {
		lastWorldChange = System.currentTimeMillis();
		chain.clear();
		cocoonPositions.clear();
		cocoonLifeTicks.clear();
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE_EVENT")
	private void onChangeIsland(@NonNull IslandType islandType, String serverName) {
		canBeTriggered = islandType != IslandType.DUNGEON
				&& islandType != IslandType.KUUDRA_HOLLOW
				&& islandType != IslandType.THE_RIFT; // Parce que dans le rift il y a les mêmes cocoons
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NonNull ArmorStand armorStandEntity, boolean equipment) {
		if (equipment || (System.currentTimeMillis() - lastWorldChange < WORLD_CHANGE_THRESHOLD_MS)) return;
		if (!isEnabled()) return;
		if (!matchesCocoonCriteria(armorStandEntity)) return;

		for (ArmorStand as : chain) {
			if (as.getId() == armorStandEntity.getId()) {
				return;
			}
		}

		chain.removeIf(a -> a.isRemoved() || a.isDeadOrDying());

		// Si la chain est vide, ce spawn devient le premier élément
		if (chain.isEmpty()) {
			chain.addLast(armorStandEntity);
			return;
		}

		// Si le nouveau spawn est assez proche d'au moins un élément de la chain, il est ajouté
		boolean closeToAny = false;
		for (ArmorStand as : chain) {
			if (as.distanceToSqr(armorStandEntity) <= MAX_ARMORSTAND_PAIR_DISTANCE_SQ) {
				closeToAny = true;
				break;
			}
		}

		if (closeToAny) {
			chain.addLast(armorStandEntity);

			// Garder au max 3 éléments pertinents dans la chain
			while (chain.size() > 3) {
				chain.removeFirst();
			}

			if (chain.size() == 3) {
				onMobCocooned(chain.element().blockPosition());
				chain.clear(); // Réinitialise la chain pour éviter de trigger à nouveau
			}
		} else {
			// Spawn trop loin, réinitialise la chain et repart à partir de ce spawn
			chain.clear();
			chain.addLast(armorStandEntity);
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVE_EVENT")
	private void onRemoveArmorStand(@NonNull ArmorStand armorStand) {
		chain.removeIf(a -> a.getId() == armorStand.getId());
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (!isEnabled()) return;

		if (!cocoonPositions.isEmpty()) {
			for (BlockPos pos : cocoonPositions) {
				final BlockPos finalPos = pos.immutable().offset(0, -4, 0);
				renderer.submitBeaconBeam(finalPos, Colors.RED);
			}
		}

		if (!cocoonLifeTicks.isEmpty()) {
			for (Map.Entry<BlockPos, Long> entry : cocoonLifeTicks.entrySet()) {
				final long finalTimeTick = entry.getValue();
				// Suppression après 8s coté client, évite un text inutile entre 6.5s (server) et 8s (client)
				if (finalTimeTick <= 0) continue;

				Component message = Component.literal(
						StonksUtils.DECIMAL_FORMAT.format(finalTimeTick / 20f) + "s"
				).withStyle(getColorFromTicks(finalTimeTick));

				Vec3 position = entry.getKey().getCenter().add(0, 2.1D, 0);
				renderer.submitText(message, position, 2f, false);
			}
		}
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (cocoonLifeTicks.isEmpty()) return;

		for (Map.Entry<BlockPos, Long> entry : cocoonLifeTicks.entrySet()) {
			long lifeTick = entry.getValue();
			if (lifeTick > 0) {
				entry.setValue(--lifeTick); // -1
			}
		}
	}

	private void onMobCocooned(BlockPos pos) {
		if (!isSlayerBossCocooned()) { // Avoir le system de Cocoon sans les alerts si le Slayer Boss est cocooned.
			Client.sendMessageWithPrefix(Component.literal(configMessage.get()));

			if (this.config().combat.cocoonedMob.cocoonedWarningSound) {
				Client.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1f, 1f);
			}

			if (this.config().combat.cocoonedMob.cocoonedWarningTitle) {
				Client.showTitle(Component.literal(configMessage.get()), 0, 27, 0);
			}
		}

		if (pos == null) return;
		final BlockPos finalPos = pos;

		if (this.config().combat.cocoonedMob.cocoonedWarningBeam) {
			// Contrairement au "cocoonedWarningTime", le beam est moins gênant
			// en cas de suppression prématuré, alors que les Time peuvent avoir un plus grand écart.
			cocoonPositions.add(pos);
			TickScheduler.getInstance().runLater(() -> cocoonPositions.remove(finalPos), 3, TimeUnit.SECONDS);
		}

		if (this.config().combat.cocoonedMob.cocoonedWarningTime) {
			// Si X mobs est cocooned au même endroit avant que le premier scheduler expire.
			// T=0s  → put(pos, TICKS), schedule remove(pos) à T+8s
			// T=3s  → put(pos, TICKS) (reset OK), schedule remove(pos) à T+10s
			// T=8s  → 1er scheduler fire → remove(pos) ← supprime le 2ème timer prématurément
			// T=10s → 2ème scheduler fire → no-op (déjà supprimé)
			if (!cocoonLifeTicks.containsKey(pos)) {
				// 8s pour rajouté en peu plus de 1s au life time des cocoons en cas de "gros" lag serveur.
				TickScheduler.getInstance().runLater(() -> cocoonLifeTicks.remove(finalPos), 8, TimeUnit.SECONDS);
			}
			cocoonLifeTicks.put(pos, COCOON_LIFE_TIME_TICKS);
		}
	}

	private boolean matchesCocoonCriteria(@NonNull ArmorStand as) {
		if (CLIENT.player == null) return false;
		if (as.isCustomNameVisible() || !as.hasItemInSlot(EquipmentSlot.HEAD)) return false;
		// Récupère la texture si présente
		String headTexture = ItemUtils.getHeadTexture(as.getItemBySlot(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) return false;
		// Check de la distance minimale
		if (CLIENT.player.position().distanceToSqr(as.position()) > MAX_PLAYER_COCOON_DISTANCE_SQ) return false;
		// Check de la texture
		return headTexture.equals(HeadTextures.COCOON);
	}

	private boolean isSlayerBossCocooned() {
		if (slayerCocoonedWarningFeature == null) return false;
		return slayerCocoonedWarningFeature.isCocoonedBoss();
	}

	private ChatFormatting getColorFromTicks(long ticks) {
		if (ticks <= 20) return ChatFormatting.RED;
		if (ticks <= 40) return ChatFormatting.GOLD;
		if (ticks <= 60) return ChatFormatting.YELLOW;
		return ChatFormatting.GREEN;
	}
}
