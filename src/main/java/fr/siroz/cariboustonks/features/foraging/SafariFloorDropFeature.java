package fr.siroz.cariboustonks.features.foraging;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SafariFloorDropFeature extends Feature {

	private final List<BlockPos> floorDrops = new ArrayList<>();

	public SafariFloorDropFeature() {
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		RenderEvents.WORLD_RENDER_EVENT.register(this::onWorldRender);
		AttackBlockCallback.EVENT.register((_, _, _, pos, _) -> this.handlePlayerInteraction(pos, null));
		UseBlockCallback.EVENT.register((_, _, _, hitResult) -> this.handlePlayerInteraction(null, hitResult));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.SAFARI
				&& this.config().foraging.safari.floorDrops;
	}

	@Override
	protected void onClientJoinServer() {
		floorDrops.clear();
	}

	@SuppressWarnings("SameReturnValue")
	@EventHandler(event = "AttackBlockCallback.EVENT && UseBlockCallback.EVENT")
	private InteractionResult handlePlayerInteraction(@Nullable BlockPos pos, @Nullable BlockHitResult blockHitResult) {
		if (!isEnabled() || floorDrops.isEmpty()) return InteractionResult.PASS;

		BlockPos target = pos != null ? pos : blockHitResult != null ? blockHitResult.getBlockPos() : null;
		if (target != null) floorDrops.remove(target);

		return InteractionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket packet) {
		if (!isEnabled()) return;
		if (!ParticleTypes.HAPPY_VILLAGER.getType().equals(packet.getParticle().getType())) return;

		BlockPos pos = BlockPos.containing(packet.getX(), packet.getY() - 1, packet.getZ());
		if (floorDrops.contains(pos)) return;

		long stringItemCount = countStringItemDisplays(pos);
		if (stringItemCount == 3 && !floorDrops.contains(pos)) {
			floorDrops.add(pos);
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void onWorldRender(WorldRenderer renderer) {
		if (!isEnabled()) return;
		if (floorDrops.isEmpty()) return;

		for (BlockPos pos : floorDrops) {
			renderer.submitBeam(Vec3.atCenterOf(pos).add(0, 0.5f, 0), Colors.GOLD, false);
		}
	}

	private long countStringItemDisplays(BlockPos pos) {
		if (MINECRAFT.level == null) return 0;

		List<Display.ItemDisplay> entities = MINECRAFT.level.getEntitiesOfClass(
				Display.ItemDisplay.class,
				AABB.ofSize(Vec3.atCenterOf(pos), 1.0, 1.0, 1.0),
				_ -> true
		);
		if (entities.isEmpty()) return 0;

		return entities.stream()
				.filter(entity -> {
					Display.ItemDisplay.ItemRenderState state = entity.itemRenderState();
					if (state == null) return false;

					ItemStack itemStack = state.itemStack();
					return !itemStack.isEmpty() && itemStack.getItem().equals(Items.STRING);
				})
				.count();
	}
}
