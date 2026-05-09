package fr.siroz.cariboustonks.features.ui.overlay;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EtherWarpOverlayFeature extends Feature {

	// Rajouter un petit Zoom quand le target est good comme en 1.8, en reprenant le code du ZoomFeature
	// en ayant un event lié entre ces 2 features ?

	private static final Set<String> ETHER_WARP_ITEMS = Set.of(
			"ASPECT_OF_THE_END",
			"ASPECT_OF_THE_VOID"
	);
	private static final int RANGE = 57;

	public EtherWarpOverlayFeature() {
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.overlay.etherWarp;
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (MINECRAFT.player == null || MINECRAFT.level == null) return;
		if (!isEnabled() || !MINECRAFT.options.keyShift.isDown()) return;

		ItemStack heldItem = PlayerContext.getMainHandItem();
		if (heldItem == null || heldItem.isEmpty()) return;

		String skyBlockItemId = SkyBlockAPI.getSkyBlockItemId(heldItem);
		CompoundTag customData = ItemUtils.getCustomData(heldItem);
		if (!ETHER_WARP_ITEMS.contains(skyBlockItemId) || customData.getIntOr("ethermerge", 0) == 0) {
			return;
		}

		int range = customData.contains("tuned_transmission")
				? RANGE + customData.getIntOr("tuned_transmission", 0)
				: RANGE;

		// Dans le cas ou le target du crosshair est valide
		HitResult hitResult = MINECRAFT.hitResult;
		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK
				&& hitResult instanceof BlockHitResult blockHitResult
				&& blockHitResult.getLocation().closerThan(MINECRAFT.player.position(), range)
		) {
			renderOverlayAt(renderer, blockHitResult);
			return;
		}

		// Sinon récupérer un HitResult depuis un rayCast
		AttributeInstance blockInteractionRange = MINECRAFT.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (blockInteractionRange != null && range > blockInteractionRange.getValue()) {

			HitResult rayCastResult = MINECRAFT.player.pick(range, RenderUtils.getTickCounter().getGameTimeDeltaPartialTick(true), false);
			if (rayCastResult.getType() == HitResult.Type.BLOCK && rayCastResult instanceof BlockHitResult blockHitResult) {
				renderOverlayAt(renderer, blockHitResult);
			}
		}
	}

	private void renderOverlayAt(WorldRenderer renderer, BlockHitResult blockHitResult) {
		if (blockHitResult == null || MINECRAFT.level == null) return;

		BlockPos pos = blockHitResult.getBlockPos();
		BlockState targetState = MINECRAFT.level.getBlockState(pos);
		BlockState targetAbove1State = MINECRAFT.level.getBlockState(pos.above());
		BlockState targetAbove2State = MINECRAFT.level.getBlockState(pos.above(2));

		if (!targetState.isAir() && targetAbove1State.isAir() && targetAbove2State.isAir()) {
			// SIROZ-NOTE: config color
			renderer.submitFilled(pos, Colors.PURPLE.withAlpha(0.75f), false);
		}
	}
}
