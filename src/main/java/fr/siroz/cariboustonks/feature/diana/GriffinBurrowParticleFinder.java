package fr.siroz.cariboustonks.feature.diana;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

final class GriffinBurrowParticleFinder {

	// TODO : MythologicalRitual V2 :: After a block attack, the handleInteractBlock is bugged

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final Cache<BlockPos, BlockPos> DUG_BURROWS_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(1))
			.build();

	private final MythologicalRitualFeature mythologicalRitual;

	private final Map<BlockPos, GriffinBurrow> burrows = new HashMap<>();

	private BlockPos lastDugParticleBurrow = null;
	private BlockPos pendingBurrow = null;

	GriffinBurrowParticleFinder(MythologicalRitualFeature mythologicalRitual) {
		this.mythologicalRitual = mythologicalRitual;

		AttackBlockCallback.EVENT.register(this::onAttackBlock);
		UseBlockCallback.EVENT.register(this::onUseBlock);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
	}

	@EventHandler(event = "AttackBlockCallback.EVENT")
	private ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
		return onInteractBlock(player, hand, pos);
	}

	@EventHandler(event = "UseBlockCallback.EVENT")
	private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, @NotNull BlockHitResult hitResult) {
		return onInteractBlock(player, hand, hitResult.getBlockPos());
	}

	private ActionResult onInteractBlock(PlayerEntity player, Hand hand, BlockPos pos) {
		if (CLIENT.world != null && !CLIENT.world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) return ActionResult.PASS;
		if (!mythologicalRitual.isEnabled() || !mythologicalRitual.isParticleFinderEnabled()) return ActionResult.PASS;
		if (!mythologicalRitual.isHoldingSpade(player.getStackInHand(hand))) return ActionResult.PASS;

		handleInteractBlock(pos);
		return ActionResult.SUCCESS;
	}

	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ParticleS2CPacket particle) {
		if (!mythologicalRitual.isEnabled() || !mythologicalRitual.isParticleFinderEnabled()) return;

		BlockPos position = BlockPos.ofFloored(particle.getX(), particle.getY(), particle.getZ()).down();
		if (CLIENT.world == null || !CLIENT.world.getBlockState(position).isOf(Blocks.GRASS_BLOCK)) {
			return;
		}

		if (DUG_BURROWS_CACHE.getIfPresent(position) != null) {
			return;
		}

		GriffinBurrow burrow = burrows.computeIfAbsent(position, GriffinBurrow::new);

		int count = particle.getCount();
		float speed = particle.getSpeed();
		float offsetX = particle.getOffsetX();
		float offsetY = particle.getOffsetY();
		float offsetZ = particle.getOffsetZ();

		switch (particle.getParameters().getType()) {
			// Type -> Start / Mob
			case ParticleType<?> type when ParticleTypes.ENCHANTED_HIT.equals(type) -> burrow.critMagicParticle++;
			// Type -> Mob
			case ParticleType<?> type when ParticleTypes.CRIT.equals(type) -> burrow.critParticle++;
			// Type -> Treasure // DRIPPING_LAVA :: count: 2 speed: 0.01 offsetX: 0.35 offsetY: 0.1 offsetZ: 0.35
			case ParticleType<?> type when ParticleTypes.DRIPPING_LAVA.equals(type)
					&& count == 2 && speed == 0.01f && offsetX == 0.35f && offsetY == 0.1f && offsetZ == 0.35f -> burrow.setType(2);
			// Enchant ? -> Start / Mob
			case ParticleType<?> type when ParticleTypes.ENCHANT.equals(type) -> burrow.enchantParticle++;
			case null, default -> {
			}
		}

		// Oui, c'est nul de faire comme ça, en 1.8 il n'y a pas besoin de "calculer" le nombre de particules
		// enchant / crit / magic crit, on peut directement vérifier comme DRIPPING_LAVA en particule, sauf
		// qu'en dernière version ça ne marche pas.
		// → À changer, du moins à optimiser plus. Comme ceci, il n'y a pas de problèmes, mais coté code,
		// je n'aime pas, trop de temps passé pour ça...

		if (burrow.critMagicParticle >= 5 && burrow.enchantParticle >= 5) { // Start
			if (!burrow.isFound()) {
				burrow.setFound(true);
				burrow.setType(0);
				mythologicalRitual.onBurrowDetected(burrow);
			}
		} else if (burrow.getType() == 2) { // Treasure
			if (!burrow.isFound()) {
				burrow.setFound(true);
				mythologicalRitual.onBurrowDetected(burrow);
			}
		} else if (burrow.critMagicParticle == 0 && burrow.critParticle >= 5 && burrow.enchantParticle >= 5) { // Mob
			if (!burrow.isFound()) {
				burrow.setFound(true);
				burrow.setType(1);
				mythologicalRitual.onBurrowDetected(burrow);
			}
		}
	}

	void resetAndClear() {
		burrows.clear();
		DUG_BURROWS_CACHE.invalidateAll();
		lastDugParticleBurrow = null;
		pendingBurrow = null;
	}

	void handleGriffinBurrowDugMessage() {
		if (!mythologicalRitual.isParticleFinderEnabled()) return;

		BlockPos burrow = lastDugParticleBurrow;
		if (burrow != null) {
			if (!commitBurrowDug(burrow, false)) {
				pendingBurrow = burrow;
			}
		}
	}

	private void handleInteractBlock(BlockPos pos) {
		if (pos.equals(pendingBurrow)) {
			pendingBurrow = null;
			commitBurrowDug(pos, true);
			return;
		}

		if (burrows.containsKey(pos)) {
			lastDugParticleBurrow = pos;
		}
	}

	private boolean commitBurrowDug(BlockPos pos, boolean ignoreFound) {
		GriffinBurrow burrow = burrows.get(pos);
		if (burrow == null || (!burrow.isFound() && !ignoreFound)) {
			return false;
		}

		burrows.remove(lastDugParticleBurrow);
		DUG_BURROWS_CACHE.put(lastDugParticleBurrow, lastDugParticleBurrow);
		// Reset
		lastDugParticleBurrow = null;
		// Le burrow est dug
		mythologicalRitual.onBurrowDug(burrow.getPos());
		return true;
	}
}
