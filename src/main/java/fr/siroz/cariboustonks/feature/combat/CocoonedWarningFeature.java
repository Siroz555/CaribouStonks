package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.slayer.SlayerCocoonedWarningFeature;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class CocoonedWarningFeature extends Feature {

	private static final long WORLD_CHANGE_THRESHOLD = 10_000;
	private static final double MAX_ARMORSTAND_PAIR_DISTANCE_SQ = 2f * 2f;
	private static final double MAX_PLAYER_COCOON_DISTANCE_SQ = 15f * 15f;
	//private static final int MAX_Y_CHECK = 6;

	private final BooleanSupplier configSoundEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningSound;

	private final BooleanSupplier configTitleEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningTitle;

	private final BooleanSupplier configBeamEnabled =
			() -> ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarningBeam;

	private final SlayerManager slayerManager;

	// chaîne temporaire des apparitions successives (ordre d'apparition)
	private final Deque<ArmorStand> chain = new ArrayDeque<>();
	private final List<BlockPos> cocoonPositions = new ArrayList<>();
	private long lastWorldChange = 0;
	private boolean canBeTriggered = false;

	public CocoonedWarningFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);
		SkyBlockEvents.ISLAND_CHANGE.register(this::onChangeIsland);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);
		RenderEvents.WORLD_RENDER.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& canBeTriggered
				// --start-- Pour détecter les cocoons depuis les Slayers Minibosses
				//&& !slayerManager.isInQuest()
				&& !slayerManager.isInQuestWithBoss(SlayerType.SPIDER)
				&& !SlayerCocoonedWarningFeature.isCocoonedBoss()
				// --end--
				&& ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarning;
	}

	@Override
	protected void onClientJoinServer() {
		lastWorldChange = System.currentTimeMillis();
		chain.clear();
		cocoonPositions.clear();
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE")
	private void onChangeIsland(@NotNull IslandType islandType) {
		canBeTriggered = islandType != IslandType.DUNGEON
				&& islandType != IslandType.KUUDRA_HOLLOW
				&& islandType != IslandType.THE_RIFT; // Parce que dans le rift il y a les mêmes cocoons
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NotNull ArmorStand armorStandEntity, boolean equipment) {
		if (equipment || (System.currentTimeMillis() - lastWorldChange < WORLD_CHANGE_THRESHOLD)) return;
		if (!isEnabled()) return;
		if (!matchesCocoonCriteria(armorStandEntity)) return;

		for (ArmorStand as : chain) {
			if (as.getId() == armorStandEntity.getId()) {
				return;
			}
		}

		chain.removeIf(a -> a.isRemoved() || a.isDeadOrDying());

		// Si la chain est vide, ce spawn devient le premier élément
		if (chain.isEmpty()) {
			chain.addLast(armorStandEntity);
			return;
		}

		// Si le nouveau spawn est assez proche d'au moins un élément de la chain, il est ajouté
		boolean closeToAny = false;
		for (ArmorStand as : chain) {
			if (as.distanceToSqr(armorStandEntity) <= MAX_ARMORSTAND_PAIR_DISTANCE_SQ) {
				closeToAny = true;
				break;
			}
		}

		if (closeToAny) {
			chain.addLast(armorStandEntity);

			// Garder au max 3 éléments pertinents dans la chain
			while (chain.size() > 3) {
				chain.removeFirst();
			}

			if (chain.size() == 3) {
				onMobCocooned(chain.element().blockPosition());
				chain.clear(); // Réinitialise la chain pour éviter de trigger à nouveau
			}
		} else {
			// Spawn trop loin, réinitialise la chain et repart à partir de ce spawn
			chain.clear();
			chain.addLast(armorStandEntity);
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVED")
	private void onRemoveArmorStand(@NotNull ArmorStand armorStand) {
		chain.removeIf(a -> a.getId() == armorStand.getId());
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		for (BlockPos pos : cocoonPositions) {
			final BlockPos finalPos = pos.immutable().offset(0, -4, 0);
			renderer.submitBeaconBeam(finalPos, Colors.RED);
		}
	}

	private void onMobCocooned(BlockPos pos) {
		Client.sendMessageWithPrefix(Component.literal("A mob has been cocooned!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

		if (configSoundEnabled.getAsBoolean()) {
			Client.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1f, 1f);
		}

		if (configTitleEnabled.getAsBoolean()) {
			Client.showTitle(Component.literal("Cocooned!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 0, 27, 0);
		}

		if (configBeamEnabled.getAsBoolean() && pos != null) {
			cocoonPositions.add(pos);
			final BlockPos finalPos = pos;
			TickScheduler.getInstance().runLater(() -> cocoonPositions.remove(finalPos), 4, TimeUnit.SECONDS);
		}
	}

	private boolean matchesCocoonCriteria(@NotNull ArmorStand as) {
		if (CLIENT.player == null) {
			return false;
		}

		// Si l'Armorstand est +/- au-dessus du joueur.
		// C'est pour empêcher la détection des cocoons des Spiders Slayer des autres joueurs,
		// Mais ça ne va pas bloquer complètement la détection.
		// SIROZ-NOTE: a voir, mais peut être check aussi if Spider Den / Crimson Isle
//		double deltaY = as.position().y() - CLIENT.player.position().y();
//		if (deltaY >= MAX_Y_CHECK) {
//			return false;
//		}

		if (as.isCustomNameVisible() || !as.hasItemInSlot(EquipmentSlot.HEAD)) {
			return false;
		}

		String headTexture = ItemUtils.getHeadTexture(as.getItemBySlot(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) {
			return false;
		}

		if (CLIENT.player.position().distanceToSqr(as.position()) > MAX_PLAYER_COCOON_DISTANCE_SQ) {
			return false;
		}

		return headTexture.equals(HeadTextures.TARANTULA_COCOON);
	}
}
