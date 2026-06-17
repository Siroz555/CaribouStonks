package fr.siroz.cariboustonks.features.ui.overlay;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.Collections;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EtherWarpOverlayFeature extends Feature {
	private static final int MAX_RANGE = 57;
	private static final Set<String> ETHER_WARP_ITEMS = Set.of(
			"ASPECT_OF_THE_END",
			"ASPECT_OF_THE_VOID"
	);
	private static final BlockMatcher<Block> INTERACTABLE_BLOCKS = new BlockMatcher<>(
			Set.of(
					Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
					Blocks.FURNACE, Blocks.BLAST_FURNACE,
					Blocks.BREWING_STAND, Blocks.CAULDRON, Blocks.WATER_CAULDRON,
					Blocks.CRAFTING_TABLE, Blocks.ENCHANTING_TABLE,
					Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER, Blocks.LEVER
			),
			Set.of(BlockTags.DOORS, BlockTags.TRAPDOORS, BlockTags.ANVIL, BlockTags.FENCE_GATES)
	);
	private static final BlockMatcher<Block> COLLISION_EXCEPTIONS = new BlockMatcher<>(
			Set.of(
					Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD,
					Blocks.DRAGON_WALL_HEAD, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL,
					Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PIGLIN_HEAD,
					Blocks.PIGLIN_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD,
					Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.REPEATER,
					Blocks.COMPARATOR, Blocks.BIG_DRIPLEAF_STEM, Blocks.MOSS_CARPET,
					Blocks.PALE_MOSS_CARPET, Blocks.COCOA, Blocks.LADDER, Blocks.SEA_PICKLE
			),
			Set.of(BlockTags.FLOWER_POTS, BlockTags.WOOL_CARPETS, BlockTags.LANTERNS)
	);
	private static final BlockMatcher<Block> ALWAYS_BLOCKING = new BlockMatcher<>(
			Collections.emptySet(),
			Set.of(BlockTags.ALL_SIGNS, BlockTags.ALL_HANGING_SIGNS, BlockTags.BANNERS)
	);

	public EtherWarpOverlayFeature() {
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.overlay.etherWarp;
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (CLIENT.player == null || CLIENT.level == null) return;

		if (!isEnabled() || !CLIENT.options.keyShift.isDown()) {
			return;
		}

		ItemStack heldItem = Client.getMainHandItem();
		if (heldItem == null || heldItem.isEmpty()) {
			return;
		}

		String skyBlockItemId = SkyBlockAPI.getSkyBlockItemId(heldItem);
		if (!ETHER_WARP_ITEMS.contains(skyBlockItemId)) {
			return;
		}

		CompoundTag customData = ItemUtils.getCustomData(heldItem);
		if (customData.getIntOr("ethermerge", 0) == 0) {
			return;
		}

		int range = MAX_RANGE + customData.getIntOr("tuned_transmission", 0);
		computeAndRenderTarget(renderer, range);
	}

	private void computeAndRenderTarget(WorldRenderer renderer, int range) {
		if (CLIENT.player == null || CLIENT.level == null) return;

		final float playerEyeHeight = Client.getEyeHeight();
		final Vec3 startPos = CLIENT.player.position().add(0, playerEyeHeight, 0);
		final Vec3 endPos = CLIENT.player.getViewVector(0f).scale(range * 2).add(startPos);

		EtherwarpBlockHit hitResult = raycast(CLIENT.level, startPos, endPos);
		if (!(hitResult instanceof EtherwarpBlockHit.BlockHit(BlockPos blockPos, Vec3 accuratePos))) {
			return;
		}

		EtherwarpResult etherwarpResult = EtherwarpResult.SUCCESS;
		if (!isEtherwarpPassable(CLIENT.level, blockPos.above())) {
			etherwarpResult = EtherwarpResult.OCCUPIED;
		} else if (!isEtherwarpPassable(CLIENT.level, blockPos.above(2))) {
			etherwarpResult = EtherwarpResult.OCCUPIED;
		} else if (startPos.distanceToSqr(accuratePos != null ? accuratePos : blockPos.getCenter()) > range * range) {
			etherwarpResult = EtherwarpResult.FAR_AWAY;
		} else if (isInteractionBlocked(CLIENT.level)) { // Vanilla HitResult
			etherwarpResult = EtherwarpResult.INTERACTION;
		}

		if (etherwarpResult.subtileText != null && this.config().uiAndVisuals.overlay.etherWarpSubtitle) {
			Client.showSubtitle(etherwarpResult.subtileText.withColor(etherwarpResult.color.asInt()), 0, 5, 0);
		}

		renderer.submitFilled(blockPos, etherwarpResult.color, false);
	}

	private @NonNull EtherwarpBlockHit raycast(BlockGetter getter, Vec3 start, Vec3 end) {
		return BlockGetter.traverseBlocks(start, end, new Object(), (o, pos) -> {
			if (isEtherwarpPassable(getter, pos)) return null;

			// Clip sur le Block Shape.
			// - length Sqr est utilisé + direction
			// - Override, comme ça on peut avoir un BlockHit et parse avec le EtherwarpResult après
			BlockHitResult result = getter.clipWithInteractionOverride(
					start, end, pos,
					Shapes.block(), getter.getBlockState(pos).getBlock().defaultBlockState()
			);
			return new EtherwarpBlockHit.BlockHit(pos, result != null ? result.getLocation() : null);
		}, o -> new EtherwarpBlockHit.Empty());
	}

	private boolean isEtherwarpPassable(@NonNull BlockGetter getter, @NonNull BlockPos pos) {
		BlockState blockState = getter.getBlockState(pos);
		Block block = blockState.getBlock();
		Holder<Block> blockHolder = blockState.getBlockHolder();

		// Impossible de passer - FAIL
		if (ALWAYS_BLOCKING.matches(blockHolder)) return false;

		// Le shape du block n'empêche pas de passer - PASS
		if (block.defaultBlockState().getCollisionShape(getter, pos).isEmpty()) return true;

		// Exception validation - PASS/FAIL
		return COLLISION_EXCEPTIONS.matches(blockHolder);
	}

	private boolean isInteractionBlocked(@NonNull BlockGetter getter) {
		HitResult hitResult = CLIENT.hitResult;
		if (hitResult instanceof BlockHitResult result && result.getType() == HitResult.Type.BLOCK) {
			return INTERACTABLE_BLOCKS.matches(getter.getBlockState(result.getBlockPos()).getBlockHolder());
		}
		return false;
	}

	private enum EtherwarpResult {
		SUCCESS(Color.fromFormatting(ChatFormatting.GREEN), null),
		INTERACTION(Color.fromFormatting(ChatFormatting.GOLD), Component.literal("!")),
		FAR_AWAY(Color.fromFormatting(ChatFormatting.DARK_PURPLE), Component.literal("!!")),
		OCCUPIED(Color.fromFormatting(ChatFormatting.RED), Component.literal("!!!"));

		final Color color;
		final @Nullable MutableComponent subtileText;

		EtherwarpResult(Color color, @Nullable MutableComponent subtileText) {
			this.color = color;
			this.subtileText = subtileText;
		}
	}

	private sealed interface EtherwarpBlockHit permits EtherwarpBlockHit.BlockHit, EtherwarpBlockHit.Empty {
		record BlockHit(BlockPos pos, @Nullable Vec3 accuratePos) implements EtherwarpBlockHit {
		}

		record Empty() implements EtherwarpBlockHit {
		}
	}

	private record BlockMatcher<T>(Set<T> direct, Set<TagKey<T>> byTag) {
		public boolean matches(@NonNull Holder<T> entry) {
			return direct.contains(entry.value()) || entry.tags().anyMatch(byTag::contains);
		}
	}
}
