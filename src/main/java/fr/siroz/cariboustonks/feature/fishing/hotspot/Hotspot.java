package fr.siroz.cariboustonks.feature.fishing.hotspot;

import java.util.Optional;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

record Hotspot(ArmorStand entity, Vec3 centerPos, Optional<String> perk) {
}
