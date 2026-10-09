package com.rezero.mod.combat;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Complete server-derived input to the Re:Zero damage pipeline. */
public record DamageContext(ServerPlayer attacker, LivingEntity target, DamageType type, float baseDamage, Identifier abilityId) {
    public DamageContext {
        if (baseDamage < 0.0F || !Float.isFinite(baseDamage)) {
            throw new IllegalArgumentException("Damage must be finite and non-negative");
        }
    }
}
