package fr.siroz.cariboustonks.feature.fishing;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record Hotspot(ArmorStandEntity entity, Vec3d centerPos, Optional<String> perk) {
}
