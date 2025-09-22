package fr.siroz.cariboustonks.feature.foraging;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.InventoryUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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

	private ArmorStandEntity currentTreeInfo = null;
	private Text currentInfo = null;

	public TreeOverlayFeature() {
		WorldEvents.JOIN.register(world -> reset());
		TickScheduler.getInstance().runRepeating(this::update, 1, TimeUnit.SECONDS);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorstandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GALATEA
				&& ConfigManager.getConfig().foraging.showTreeOverlayInfo;
	}

	private void update() {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled()) return;

		ItemStack heldItem = InventoryUtils.getMainHandItem();
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
			Optional<ArmorStandEntity> newTreeInfo = findClosestTreeInfoInRange(CLIENT.player);
			if (newTreeInfo.isEmpty()) {
				reset();
			}
		}

		if (currentInfo != null) {
			Client.showSubtitle(currentInfo, 0, 60, 20);
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorstandUpdate(@NotNull ArmorStandEntity entity, boolean b) {
		if (!isEnabled()) return;
		if (currentTreeInfo == null) return;

		if (entity.getX() == currentTreeInfo.getX()
				&& entity.getY() > currentTreeInfo.getY()
				&& entity.getZ() == currentTreeInfo.getZ()) {
			currentInfo = entity.getName();
		}
	}

	private void reset() {
		currentInfo = null;
		currentTreeInfo = null;
	}

	private Optional<ArmorStandEntity> findClosestTreeInfoInRange(@Nullable Entity entity) {
		if (CLIENT.world == null || CLIENT.player == null || entity == null) {
			return Optional.empty();
		}

		List<ArmorStandEntity> armorStands = CLIENT.world.getEntitiesByClass(
				ArmorStandEntity.class,
				entity.getBoundingBox().expand(DISTANCE_TO_TREE_INFO_IN_BLOCKS),
				Entity::hasCustomName
		);

		ArmorStandEntity closestTreeInfoArmorStand = armorStands.stream()
				.filter(as -> as.getName().getString().contains(CLIENT.getSession().getUsername()))
				.min(Comparator.comparingDouble(as -> as.squaredDistanceTo(entity)))
				.orElse(null);

		if (closestTreeInfoArmorStand == null) {
			return Optional.empty();
		}

		return Optional.of(closestTreeInfoArmorStand);
	}
}
