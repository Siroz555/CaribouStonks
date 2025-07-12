package fr.siroz.cariboustonks.feature.ui.overlay;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRenderer;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class EtherWarpOverlayFeature extends Feature implements WorldRenderer {

	// Rajouter un petit Zoom quand le target est good comme en 1.8, en reprenant le code du ZoomFeature
	// en ayant un event lié entre ces 2 features ?

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final Set<String> ETHER_WARP_ITEMS = Set.of(
			"ASPECT_OF_THE_END",
			"ASPECT_OF_THE_VOID"
	);
	private static final int RANGE = 57;

	public EtherWarpOverlayFeature() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.overlay.etherWarp;
	}

	@Override
	public void render(WorldRenderContext context) {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled() || !CLIENT.options.sneakKey.isPressed()) return;

		ItemStack heldItem = CLIENT.player.getMainHandStack();
		if (heldItem == null || heldItem.isEmpty()) {
			return;
		}

		String skyBlockItemId = ItemUtils.getSkyBlockItemId(heldItem);
		NbtCompound customData = ItemUtils.getCustomData(heldItem);
		if (!ETHER_WARP_ITEMS.contains(skyBlockItemId) || customData.getInt("ethermerge", 0) == 0) {
			return;
		}

		int range = customData.contains("tuned_transmission")
				? RANGE + customData.getInt("tuned_transmission", 0)
				: RANGE;

		// Dans le cas ou le target du crosshair est valide
		HitResult hitResult = CLIENT.crosshairTarget;
		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK
				&& hitResult instanceof BlockHitResult blockHitResult
				&& blockHitResult.getPos().isInRange(CLIENT.player.getPos(), range)
		) {
			renderOverlayAt(context, blockHitResult);
			return;
		}

		// Sinon récupérer un HitResult depuis un rayCast
		EntityAttributeInstance blockInteractionRange = CLIENT.player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
		if (blockInteractionRange != null && range > blockInteractionRange.getValue()) {

			HitResult rayCastResult = CLIENT.player.raycast(range, context.tickCounter().getTickProgress(true), false);
			if (rayCastResult.getType() == HitResult.Type.BLOCK && rayCastResult instanceof BlockHitResult blockHitResult) {
				renderOverlayAt(context, blockHitResult);
			}
		}
	}

	private void renderOverlayAt(WorldRenderContext context, BlockHitResult blockHitResult) {
		if (blockHitResult == null || CLIENT.world == null) return;

		BlockPos pos = blockHitResult.getBlockPos();
		BlockState targetState = CLIENT.world.getBlockState(pos);
		BlockState targetAbove1State = CLIENT.world.getBlockState(pos.up());
		BlockState targetAbove2State = CLIENT.world.getBlockState(pos.up(2));

		if (!targetState.isAir() && targetAbove1State.isAir() && targetAbove2State.isAir()) {
			WorldRenderUtils.renderFilled(context, pos, Colors.PURPLE, 0.75f, false);
		}
	}
}
