package fr.siroz.cariboustonks.feature.foraging;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import org.jetbrains.annotations.NotNull;

public class BreakTreeAnimationFeature extends Feature {

	private final Set<Block> treeBlocks = Set.of(
			Blocks.STRIPPED_SPRUCE_WOOD,
			Blocks.AZALEA_LEAVES,
			Blocks.MANGROVE_WOOD,
			Blocks.MANGROVE_LEAVES
	);

	public BreakTreeAnimationFeature() {
		RenderEvents.RENDER_ENTITY_CANCELLABLE.register(this::onRenderEntity);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GALATEA
				&& ConfigManager.getConfig().foraging.hideTreeBreakAnimation;
	}

	@EventHandler(event = "RenderEvents.RENDER_ENTITY")
	private boolean onRenderEntity(@NotNull Entity entity) {
		if (!isEnabled()) return false;
		if (!(entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity)) return false;

		return treeBlocks.contains(blockDisplayEntity.getBlockState().getBlock());
	}
}
