package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRendererProvider;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class CocoonedWarningFeature extends Feature implements WorldRendererProvider {

	private static final long WORLD_CHANGE_THRESHOLD = 10_000;
	private static final double MAX_DISTANCE_SQ = 2f * 2f;

	private final Supplier<Boolean> configSoundEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningSound;

	private final Supplier<Boolean> configTitleEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningTitle;

	private final Supplier<Boolean> configBeamEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningBeam;

	private final SlayerManager slayerManager;

	// chaîne temporaire des apparitions successives (ordre d'apparition)
	private final Deque<ArmorStandEntity> chain = new ArrayDeque<>();
	private final List<BlockPos> cocoonPositions = new ArrayList<>();
	private long lastWorldChange = 0;
	private boolean canBeTriggered = false;

	public CocoonedWarningFeature(SlayerManager slayerManager) {
		this.slayerManager = slayerManager;
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this::onChangeWorld);
		SkyBlockEvents.ISLAND_CHANGE.register(this::onChangeIsland);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& canBeTriggered
				&& !slayerManager.isInQuest()
				&& ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarning;
	}

	@EventHandler(event = "ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE")
	private void onChangeWorld(MinecraftClient _mc, ClientWorld _world) {
		lastWorldChange = System.currentTimeMillis();
		chain.clear();
		cocoonPositions.clear();
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE")
	private void onChangeIsland(@NotNull IslandType islandType) {
		canBeTriggered = islandType != IslandType.DUNGEON && islandType != IslandType.KUUDRA_HOLLOW;
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NotNull ArmorStandEntity armorStandEntity, boolean equipment) {
		if (equipment || (System.currentTimeMillis() - lastWorldChange < WORLD_CHANGE_THRESHOLD)) return;
		if (!isEnabled()) return;
		if (!isCocoon(armorStandEntity)) return;

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
			if (as.squaredDistanceTo(armorStandEntity) <= MAX_DISTANCE_SQ) {
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

	@Override
	public void render(WorldRenderContext context) {
		for (BlockPos pos : cocoonPositions) {
			final BlockPos finalPos = pos.toImmutable().add(0, -4, 0);
			WorldRenderUtils.renderBeaconBeam(context, finalPos, Colors.RED);
		}
	}

	private void onMobCocooned(BlockPos pos) {
		Client.sendMessageWithPrefix(Text.literal("You cocooned a Mob!").formatted(Formatting.RED, Formatting.BOLD));

		if (configSoundEnabled.get()) {
			Client.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1f);
		}

		if (configTitleEnabled.get()) {
			Client.showTitle(Text.literal("Cocooned!").formatted(Formatting.RED, Formatting.BOLD), 0, 27, 0);
		}

		if (configBeamEnabled.get() && pos != null) {
			cocoonPositions.add(pos);
			final BlockPos finalPos = pos;
			TickScheduler.getInstance().runLater(() -> cocoonPositions.remove(finalPos), 4, TimeUnit.SECONDS);
		}
	}

	private boolean isCocoon(@NotNull ArmorStandEntity as) {
		if (as.isCustomNameVisible() || !as.hasStackEquipped(EquipmentSlot.HEAD)) {
			return false;
		}

		String headTexture = ItemUtils.getHeadTexture(as.getEquippedStack(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) {
			return false;
		}

		return headTexture.equals(HeadTextures.TARANTULA_COCOON);
	}
}
