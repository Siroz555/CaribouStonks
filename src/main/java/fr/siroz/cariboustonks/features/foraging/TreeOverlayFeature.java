package fr.siroz.cariboustonks.features.foraging;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.context.WorldContext;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

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
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorstandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GALATEA
				&& this.config().foraging.showTreeOverlayInfo;
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@Override
	protected void onSecondPassed() {
		if (MINECRAFT.player == null || MINECRAFT.level == null) return;
		if (!isEnabled()) return;

		ItemStack heldItem = PlayerContext.getMainHandItem();
		if (heldItem == null) return;

		String itemId = SkyBlockAPI.getSkyBlockItemId(heldItem);
		if (!AXES.contains(itemId)) return;

		if (currentTreeInfo == null) {
			currentTreeInfo = findClosestTreeInfoInRange().orElse(null);
		} else {
			Optional<ArmorStand> newTreeInfo = findClosestTreeInfoInRange();
			if (newTreeInfo.isEmpty()) {
				reset();
			}
		}

		if (currentInfo != null) {
			PlayerContext.showSubtitle(currentInfo, 0, 60, 20);
		}
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorstandUpdate(@NonNull ArmorStand entity, boolean b) {
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

	private Optional<ArmorStand> findClosestTreeInfoInRange() {
		if (MINECRAFT.level == null || MINECRAFT.player == null) return Optional.empty();

		ArmorStand closestTreeInfoArmorStand = WorldContext.findClosestEntity(
				ArmorStand.class,
				DISTANCE_TO_TREE_INFO_IN_BLOCKS,
				Entity::hasCustomName,
				as -> as.getName().getString().contains(ClientContext.getPlayerName())
		);
		if (closestTreeInfoArmorStand == null) {
			return Optional.empty();
		}

		return Optional.of(closestTreeInfoArmorStand);
	}
}
