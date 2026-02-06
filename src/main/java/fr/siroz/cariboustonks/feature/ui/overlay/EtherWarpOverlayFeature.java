package fr.siroz.cariboustonks.feature.ui.overlay;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
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
		RenderEvents.WORLD_RENDER.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.overlay.etherWarp;
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!isEnabled() || !CLIENT.options.keyShift.isDown()) return;

		ItemStack heldItem = CLIENT.player.getMainHandItem();
		if (heldItem.isEmpty()) {
			return;
		}

		String skyBlockItemId = SkyBlockAPI.getSkyBlockItemId(heldItem);
		CompoundTag customData = ItemUtils.getCustomData(heldItem);
		if (!ETHER_WARP_ITEMS.contains(skyBlockItemId) || customData.getIntOr("ethermerge", 0) == 0) {
			return;
		}

		int range = customData.contains("tuned_transmission")
				? RANGE + customData.getIntOr("tuned_transmission", 0)
				: RANGE;

		// Dans le cas ou le target du crosshair est valide
		HitResult hitResult = CLIENT.hitResult;
		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK
				&& hitResult instanceof BlockHitResult blockHitResult
				&& blockHitResult.getLocation().closerThan(CLIENT.player.position(), range)
		) {
			renderOverlayAt(renderer, blockHitResult);
			return;
		}

		// Sinon récupérer un HitResult depuis un rayCast
		AttributeInstance blockInteractionRange = CLIENT.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (blockInteractionRange != null && range > blockInteractionRange.getValue()) {

			HitResult rayCastResult = CLIENT.player.pick(range, RenderUtils.getTickCounter().getGameTimeDeltaPartialTick(true), false);
			if (rayCastResult.getType() == HitResult.Type.BLOCK && rayCastResult instanceof BlockHitResult blockHitResult) {
				renderOverlayAt(renderer, blockHitResult);
			}
		}
	}

	private void renderOverlayAt(WorldRenderer renderer, BlockHitResult blockHitResult) {
		if (blockHitResult == null || CLIENT.level == null) return;

		BlockPos pos = blockHitResult.getBlockPos();
		BlockState targetState = CLIENT.level.getBlockState(pos);
		BlockState targetAbove1State = CLIENT.level.getBlockState(pos.above());
		BlockState targetAbove2State = CLIENT.level.getBlockState(pos.above(2));

		if (!targetState.isAir() && targetAbove1State.isAir() && targetAbove2State.isAir()) {
			renderer.submitFilled(pos, Colors.PURPLE.withAlpha(0.75f), false);
		}
	}
}
