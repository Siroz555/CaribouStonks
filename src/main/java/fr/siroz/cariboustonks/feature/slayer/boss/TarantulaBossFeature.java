package fr.siroz.cariboustonks.feature.slayer.boss;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.glowing.EntityGlowProvider;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.manager.slayer.SlayerTier;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRendererProvider;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TarantulaBossFeature extends Feature implements WorldRendererProvider, EntityGlowProvider {

	private static final Pattern COCOON_EGG_PATTERN = Pattern.compile("(\\d+)s (\\d)/3");

	private final SlayerManager slayerManager;
	private final Set<ArmorStandEntity> bossEggs = new HashSet<>();

	public TarantulaBossFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);;
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((_mc, _world) -> this.bossEggs.clear());
		SkyBlockEvents.SLAYER_BOSS_DEATH.register((_type, _tier, _startTime) -> this.bossEggs.clear());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
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
	public void render(WorldRenderContext context) {
		if (!isEnabled()) return;
		if (bossEggs.isEmpty()) return;
		if (!ConfigManager.getConfig().slayer.tarantulaBoss.showCursorLineToBossEggs) return;

		for (ArmorStandEntity egg : bossEggs) {
			WorldRenderUtils.renderLineFromCursor(context, egg.getPos(), Colors.PURPLE, 1.2f);
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
				if (bossEntity != null && armorStand.getPos().distanceTo(bossEntity.getPos()) <= 15) {
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
