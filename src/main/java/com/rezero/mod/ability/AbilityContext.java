package com.rezero.mod.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Validated, server-derived context passed to an ability. */
public record AbilityContext(ServerPlayer caster, Entity target, Vec3 direction) {
}
