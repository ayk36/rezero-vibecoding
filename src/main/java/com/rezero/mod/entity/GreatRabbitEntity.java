package com.rezero.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.Level;

/** Lightweight Great Rabbit member. Population mechanics are managed separately, not through huge entity counts. */
public final class GreatRabbitEntity extends Rabbit {
    public GreatRabbitEntity(EntityType<? extends Rabbit> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Rabbit.createAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }
}
