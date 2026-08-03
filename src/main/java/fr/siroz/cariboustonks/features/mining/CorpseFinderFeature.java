package fr.siroz.cariboustonks.features.mining;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class CorpseFinderFeature extends Feature {
	private static final double PROXIMITY_THRESHOLD_SQ = 5 * 5;

	private final Map<Integer, Corpse> corpses = new HashMap<>();

	public CorpseFinderFeature() {
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((_mc, _level) -> this.corpses.clear());
		RenderEvents.WORLD_RENDER_EVENT.register(this::onWorldRender);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GLACITE_MINESHAFT
				&& this.config().mining.mineshaft.corpseFinder;
	}

	@Override
	protected void onSecondPassed() {
		if (!isEnabled()) return;
		if (corpses.isEmpty()) return;

		for (Corpse corpse : corpses.values()) {
			if (corpse.found) continue;
			if (Client.getCurrentPosition().distToCenterSqr(corpse.pos) <= PROXIMITY_THRESHOLD_SQ) {
				corpse.found = true;
			}
		}
	}

	@EventHandler(event = "ClientEntityEvents.ENTITY_LOAD")
	private void onArmorStandUpdate(@NonNull ArmorStand as, boolean equipment) {
		if (!isEnabled()) return;
		if (as.hasCustomName()) return;
		if (corpses.containsKey(as.getId())) return;

		String skyBlockItemId = SkyBlockAPI.getSkyBlockItemId(as.getItemBySlot(EquipmentSlot.HEAD));
		CorpseType corpseType = CorpseType.from(skyBlockItemId);
		if (corpseType != CorpseType.UNKNOWN) {
			corpses.put(as.getId(), new Corpse(as.position(), corpseType, false));
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void onWorldRender(WorldRenderer renderer) {
		if (!isEnabled()) return;
		if (corpses.isEmpty()) return;

		for (Corpse corpse : corpses.values()) {
			if (corpse.found) continue;
			renderer.submitOutline(new AABB(corpse.pos, corpse.pos.add(1, 1, 1)), corpse.type.color, 1f, true);
		}
	}

	private static class Corpse {
		final Vec3 pos;
		final CorpseType type;
		boolean found;

		Corpse(Vec3 pos, CorpseType type, boolean found) {
			this.pos = pos;
			this.type = type;
			this.found = found;
		}
	}

	enum CorpseType {
		LAPIS("LAPIS_ARMOR_HELMET", Colors.BLUE.withAlpha(1f)),
		UMBER("ARMOR_OF_YOG_HELMET", Colors.YELLOW.withAlpha(1f)),
		TUNGSTEN("MINERAL_HELMET", Colors.GRAY.withAlpha(1f)),
		VANGUARD("VANGUARD_HELMET", Colors.AQUA.withAlpha(1f)),
		UNKNOWN("UNKNOWN", Colors.RED);

		private static final CorpseType[] VALUES = values();

		final String skyBlockItemId;
		final Color color;

		CorpseType(String skyBlockItemId, Color color) {
			this.skyBlockItemId = skyBlockItemId;
			this.color = color;
		}

		static CorpseType from(String skyBlockItemId) {
			if (skyBlockItemId == null || skyBlockItemId.isEmpty()) return UNKNOWN;

			for (CorpseType corpseType : VALUES) {
				if (corpseType.skyBlockItemId.equals(skyBlockItemId)) {
					return corpseType;
				}
			}
			return UNKNOWN;
		}
	}
}
