package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

@Deprecated // TODO - Draft
public class CocoonedWarningFeature extends Feature {

	private static final long WORLD_CHANGE_THRESHOLD = 10_000;
	private static final double MAX_DISTANCE_SQ = 2f * 2f;

	private final SlayerManager slayerManager;

	// chaîne temporaire des apparitions successives (ordre d'apparition)
	private final Deque<ArmorStandEntity> chain = new ArrayDeque<>();
	private long lastWorldChange = 0;

	public CocoonedWarningFeature(SlayerManager slayerManager) {
		this.slayerManager = slayerManager;
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this::onChangeWorld);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onUpdateArmorStand);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& !slayerManager.isInQuest()
				&& ConfigManager.getConfig().combat.cocoonedMob.cocoonedWarning;
	}

	@EventHandler(event = "ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE")
	private void onChangeWorld(MinecraftClient _mc, ClientWorld _world) {
		lastWorldChange = System.currentTimeMillis();
		chain.clear();
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onUpdateArmorStand(@NotNull ArmorStandEntity armorStandEntity, boolean equipment) {
		if (equipment || (System.currentTimeMillis() - lastWorldChange < WORLD_CHANGE_THRESHOLD)) return;
		if (!isEnabled()) return;
		if (!isCocoon(armorStandEntity)) return;

		for (ArmorStandEntity as : chain) {
			if (as.getId() == armorStandEntity.getId()) {
				return;
			}
		}

		chain.removeIf(a -> a.isRemoved() || a.isDead());

		// Si la chain est vide, ce spawn devient le premier élément
		if (chain.isEmpty()) {
			chain.addLast(armorStandEntity);
			return;
		}

		// Si le nouveau spawn est assez proche d'au moins un élément de la chain, il est ajouté
		boolean closeToAny = false;
		for (ArmorStandEntity as : chain) {
			if (as.squaredDistanceTo(armorStandEntity) <= MAX_DISTANCE_SQ) {
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
				onReincarnationDetected();
				chain.clear(); // Réinitialise la chain pour éviter de trigger à nouveau
			}
		} else {
			// Spawn trop loin, réinitialise la chain et repart à partir de ce spawn
			chain.clear();
			chain.addLast(armorStandEntity);
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVED")
	private void onRemoveArmorStand(@NotNull ArmorStandEntity armorStand) {
		chain.removeIf(a -> a.getId() == armorStand.getId());
	}

	private void onReincarnationDetected() {
		Client.sendMessageWithPrefix(Text.literal("A reincarnation has been detected!").formatted(Formatting.RED));
		Client.showTitle(Text.literal("Cocooned!").formatted(Formatting.RED, Formatting.BOLD), 0, 40, 0);
		Client.playSoundNotificationChime();
	}

	private boolean isCocoon(@NotNull ArmorStandEntity as) {
		if (as.isCustomNameVisible() || !as.hasStackEquipped(EquipmentSlot.HEAD)) {
			return false;
		}

		String headTexture = ItemUtils.getHeadTexture(as.getEquippedStack(EquipmentSlot.HEAD));
		if (headTexture.isBlank()) {
			return false;
		}

		return headTexture.equals(HeadTextures.TARANTULA_COCOON);
	}
}
