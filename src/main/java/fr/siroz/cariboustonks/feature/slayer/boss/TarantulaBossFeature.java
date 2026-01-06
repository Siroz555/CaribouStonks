package fr.siroz.cariboustonks.feature.slayer.boss;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.glowing.EntityGlowProvider;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.manager.slayer.SlayerTier;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TarantulaBossFeature extends Feature implements EntityGlowProvider {

	private static final Pattern COCOON_EGG_PATTERN = Pattern.compile("(\\d+)s (\\d)/3");

	private final SlayerManager slayerManager;
	private final Set<ArmorStandEntity> bossEggs = new HashSet<>();

	public TarantulaBossFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);
		SkyBlockEvents.SLAYER_BOSS_END.register((_type, _tier, _startTime) -> this.bossEggs.clear());
		RenderEvents.WORLD_RENDER.register(this::render);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
		WorldEvents.ARMORSTAND_REMOVED.register(this::onRemoveArmorStand);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().slayer.tarantulaBoss.highlightBossEggs
				&& slayerManager.isInQuestWithBoss(SlayerType.SPIDER)
				&& slayerManager.isSlayerTier(SlayerTier.V);
	}

	@Override
	protected void onClientJoinServer() {
		bossEggs.clear();
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!isEnabled()) return;
		if (bossEggs.isEmpty()) return;
		if (!ConfigManager.getConfig().slayer.tarantulaBoss.showCursorLineToBossEggs) return;

		for (ArmorStandEntity egg : bossEggs) {
			renderer.submitLineFromCursor(egg.getEntityPos(), Colors.PURPLE, 1.2f);
		}
	}

	@Override
	public int getEntityGlowColor(@NotNull Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand && isTarantulaBossEgg(armorStand)) {
			return ConfigManager.getConfig().slayer.tarantulaBoss.highlightBossEggsColor.getRGB();
		}

		return DEFAULT;
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NotNull ArmorStandEntity armorStand, boolean equipment) {
		if (isEnabled() && !equipment) {
			Matcher cocoonEggMatcher = COCOON_EGG_PATTERN.matcher(armorStand.getName().getString());
			if (cocoonEggMatcher.matches()) {
				Entity bossEntity = slayerManager.getBossEntity();
				if (bossEntity != null && armorStand.getEntityPos().distanceTo(bossEntity.getEntityPos()) <= 15) {
					bossEggs.add(armorStand);
				}
			}
		}
	}

	@EventHandler(event = "WorldEvents.ARMORSTAND_REMOVED")
	private void onRemoveArmorStand(@NotNull ArmorStandEntity armorStand) {
		if (isEnabled() && !bossEggs.isEmpty()) {
			Iterator<ArmorStandEntity> iterator = bossEggs.iterator();
			while (iterator.hasNext()) {
				ArmorStandEntity egg = iterator.next();
				if (egg.getId() == armorStand.getId()) {
					iterator.remove();
					break;
				}
			}
		}
	}

	private boolean isTarantulaBossEgg(@NotNull ArmorStandEntity as) {
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
