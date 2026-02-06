package fr.siroz.cariboustonks.feature.foraging;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.RenderEvents;
import java.util.Set;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class BreakTreeAnimationFeature extends Feature {

	private final Set<Block> treeBlocks = Set.of(
			Blocks.STRIPPED_SPRUCE_WOOD,
			Blocks.AZALEA_LEAVES,
			Blocks.MANGROVE_WOOD,
			Blocks.MANGROVE_LEAVES
	);

	public BreakTreeAnimationFeature() {
		RenderEvents.ALLOW_RENDER_ENTITY.register(this::allowRenderEntity);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GALATEA
				&& this.config().foraging.hideTreeBreakAnimation;
	}

	@EventHandler(event = "RenderEvents.ALLOW_RENDER_ENTITY")
	private boolean allowRenderEntity(@NotNull Entity entity) {
		if (!isEnabled()) return true;
		if (!(entity instanceof Display.BlockDisplay blockDisplayEntity)) return true;

		return !treeBlocks.contains(blockDisplayEntity.getBlockState().getBlock());
	}
}
