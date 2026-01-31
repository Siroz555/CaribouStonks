package fr.siroz.cariboustonks.feature.foraging;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeOverlayFeature extends Feature {

	private static final int DISTANCE_TO_TREE_INFO_IN_BLOCKS = 10;
	private static final Set<String> AXES = Set.of(
			"ROOKIE_AXE",
			"SWEET_AXE",
			"PROMISING_AXE",
			"EFFICIENT_AXE",
			"JUNGLE_AXE",
			"TREECAPITATOR_AXE",
			"FIG_AXE",
			"FIGSTONE_AXE"
	);

	private ArmorStand currentTreeInfo = null;
	private Component currentInfo = null;

	public TreeOverlayFeature() {
		TickScheduler.getInstance().runRepeating(this::update, 1, TimeUnit.SECONDS);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorstandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GALATEA
				&& ConfigManager.getConfig().foraging.showTreeOverlayInfo;
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	private void update() {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!isEnabled()) return;

		ItemStack heldItem = Client.getMainHandItem();
		if (heldItem == null) {
			return;
		}

		String itemId = SkyBlockAPI.getSkyBlockItemId(heldItem);
		if (!AXES.contains(itemId)) {
			return;
		}

		if (currentTreeInfo == null) {
			currentTreeInfo = findClosestTreeInfoInRange(CLIENT.player).orElse(null);
		} else {
			Optional<ArmorStand> newTreeInfo = findClosestTreeInfoInRange(CLIENT.player);
			if (newTreeInfo.isEmpty()) {
				reset();
			}
		}

		if (currentInfo != null) {
			Client.showSubtitle(currentInfo, 0, 60, 20);
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorstandUpdate(@NotNull ArmorStand entity, boolean b) {
		if (!isEnabled()) return;
		if (currentTreeInfo == null) return;

		if (entity.getX() == currentTreeInfo.getX()
				&& entity.getY() > currentTreeInfo.getY()
				&& entity.getZ() == currentTreeInfo.getZ()
		) {
			currentInfo = entity.getName();
		}
	}

	private void reset() {
		currentInfo = null;
		currentTreeInfo = null;
	}

	private Optional<ArmorStand> findClosestTreeInfoInRange(@Nullable Entity entity) {
		if (CLIENT.level == null || CLIENT.player == null || entity == null) {
			return Optional.empty();
		}

		List<ArmorStand> armorStands = CLIENT.level.getEntitiesOfClass(
				ArmorStand.class,
				entity.getBoundingBox().inflate(DISTANCE_TO_TREE_INFO_IN_BLOCKS),
				Entity::hasCustomName
		);

		ArmorStand closestTreeInfoArmorStand = armorStands.stream()
				.filter(as -> as.getName().getString().contains(CLIENT.getUser().getName()))
				.min(Comparator.comparingDouble(as -> as.distanceToSqr(entity)))
				.orElse(null);

		if (closestTreeInfoArmorStand == null) {
			return Optional.empty();
		}

		return Optional.of(closestTreeInfoArmorStand);
	}
}
