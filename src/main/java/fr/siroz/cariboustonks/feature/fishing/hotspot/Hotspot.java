package fr.siroz.cariboustonks.feature.fishing.hotspot;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

record Hotspot(ArmorStandEntity entity, Vec3d centerPos, Optional<String> perk) {
}
