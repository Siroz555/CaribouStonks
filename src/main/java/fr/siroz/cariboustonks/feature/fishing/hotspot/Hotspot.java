package fr.siroz.cariboustonks.feature.fishing.hotspot;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

record Hotspot(ArmorStand entity, Vec3 centerPos, Optional<String> perk) {
}
