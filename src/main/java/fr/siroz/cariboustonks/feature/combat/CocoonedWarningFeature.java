package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.slayer.SlayerCocoonedWarningFeature;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.Ticks;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class CocoonedWarningFeature extends Feature {

	private static final long WORLD_CHANGE_THRESHOLD = 10_000;
	private static final double MAX_ARMORSTAND_PAIR_DISTANCE_SQ = 2f * 2f;
	private static final double MAX_PLAYER_COCOON_DISTANCE_SQ = 15f * 15f;
	private static final long COCOON_LIFE_TIME_TICKS = Ticks.from(6500, TimeUnit.MILLISECONDS); // 6.5s

	private final BooleanSupplier configSoundEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningSound;

	private final BooleanSupplier configTitleEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningTitle;

	private final BooleanSupplier configBeamEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningBeam;

	private final BooleanSupplier configTimeEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningTime;

	private final Supplier<String> configMessage =
			() -> ConfigManager.getConfig().combat.cocoonedMob.message;

	private final SlayerManager slayerManager;

	// chaîne temporaire des apparitions successives (ordre d'apparition)
	private final Deque<ArmorStandEntity> chain = new ArrayDeque<>();
	private final List<BlockPos> cocoonPositions = new ArrayList<>();
	private final Map<BlockPos, Long> cocoonLifeTicks = new ConcurrentHashMap<>();
	private long lastWorldChange = 0;
	private boolean canBeTriggered = false;

	public CocoonedWarningFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);
		SkyBlockEvents.ISLAND_CHANGE.register(this::onChangeIsland);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);
		RenderEvents.WORLD_RENDER.register(this::render);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& canBeTriggered
				&& !slayerManager.isInQuestWithBoss(SlayerType.SPIDER)
				&& ConfigManager.getConfig().combat.cocoonedMob.warning;
	}

	@Override
	protected void onClientJoinServer() {
		lastWorldChange = System.currentTimeMillis();
		chain.clear();
		cocoonPositions.clear();
		cocoonLifeTicks.clear();
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE")
	private void onChangeIsland(@NotNull IslandType islandType, String serverName) {
		canBeTriggered = islandType != IslandType.DUNGEON
				&& islandType != IslandType.KUUDRA_HOLLOW
				&& islandType != IslandType.THE_RIFT; // Parce que dans le rift il y a les mêmes cocoons
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NotNull ArmorStandEntity armorStandEntity, boolean equipment) {
		if (equipment || (System.currentTimeMillis() - lastWorldChange < WORLD_CHANGE_THRESHOLD)) return;
		if (!isEnabled()) return;
		if (!matchesCocoonCriteria(armorStandEntity)) return;

		for (ArmorStandEntity as : chain) {
			if (as.getId() == armorStandEntity.getId()) {
				return;
			}
		}

		chain.removeIf(a -> a.isRemoved() || a.isDead());

		// Si la chain est vide, ce spawn devient le premier élément
		if (chain.isEmpty()) {
			chain.addLast(armorStandEntity);
			return;
		}

		// Si le nouveau spawn est assez proche d'au moins un élément de la chain, il est ajouté
		boolean closeToAny = false;
		for (ArmorStandEntity as : chain) {
			if (as.squaredDistanceTo(armorStandEntity) <= MAX_ARMORSTAND_PAIR_DISTANCE_SQ) {
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
				onMobCocooned(chain.element().getBlockPos());
				chain.clear(); // Réinitialise la chain pour éviter de trigger à nouveau
			}
		} else {
			// Spawn trop loin, réinitialise la chain et repart à partir de ce spawn
			chain.clear();
			chain.addLast(armorStandEntity);
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVED")
	private void onRemoveArmorStand(@NotNull ArmorStandEntity armorStand) {
		chain.removeIf(a -> a.getId() == armorStand.getId());
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!isEnabled()) return;

		if (!cocoonPositions.isEmpty()) {
			for (BlockPos pos : cocoonPositions) {
				final BlockPos finalPos = pos.toImmutable().add(0, -4, 0);
				renderer.submitBeaconBeam(finalPos, Colors.RED);
			}
		}

		if (!cocoonLifeTicks.isEmpty()) {
			for (Map.Entry<BlockPos, Long> entry : cocoonLifeTicks.entrySet()) {
				final long finalTimeTick = entry.getValue();
				// Suppression après 8s coté client, évite un text inutile entre 6.5s (server) et 8s (client)
				if (finalTimeTick <= 0) continue;

				Text message = Text.literal(
						StonksUtils.DECIMAL_FORMAT.format(finalTimeTick / 20f) + "s"
				).formatted(getColorFromTicks(finalTimeTick));

				Vec3d position = entry.getKey().toCenterPos().add(0, 2.1D, 0);
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
			Client.sendMessageWithPrefix(Text.literal(configMessage.get()));

			if (configSoundEnabled.getAsBoolean()) {
				Client.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1f);
			}

			if (configTitleEnabled.getAsBoolean()) {
				Client.showTitle(Text.literal(configMessage.get()), 0, 27, 0);
			}
		}

		if (pos == null) return;
		final BlockPos finalPos = pos;

		if (configBeamEnabled.getAsBoolean()) {
			// Contrairement au "cocoonedWarningTime", le beam est moins gênant
			// en cas de suppression prématuré, alors que les Time peuvent avoir un plus grand écart.
			cocoonPositions.add(pos);
			TickScheduler.getInstance().runLater(() -> cocoonPositions.remove(finalPos), 3, TimeUnit.SECONDS);
		}

		if (configTimeEnabled.getAsBoolean()) {
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

	private boolean matchesCocoonCriteria(@NotNull ArmorStandEntity as) {
		if (as.isCustomNameVisible() || !as.hasStackEquipped(EquipmentSlot.HEAD)) {
			return false;
		}

		String headTexture = ItemUtils.getHeadTexture(as.getEquippedStack(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) {
			return false;
		}

		if (CLIENT.player != null && CLIENT.player.getEntityPos().squaredDistanceTo(as.getEntityPos()) > MAX_PLAYER_COCOON_DISTANCE_SQ) {
			return false;
		}

		return headTexture.equals(HeadTextures.TARANTULA_COCOON);
	}

	private boolean isSlayerBossCocooned() {
		return SlayerCocoonedWarningFeature.isCocoonedBoss();
	}

	private Formatting getColorFromTicks(long ticks) {
		if (ticks <= 20) return Formatting.RED;
		if (ticks <= 40) return Formatting.GOLD;
		if (ticks <= 60) return Formatting.YELLOW;
		return Formatting.GREEN;
	}
}
