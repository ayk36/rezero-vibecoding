package com.rezero.mod.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BossPhase(String id, float healthThreshold) {
    public static final Codec<BossPhase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(BossPhase::id),
            Codec.FLOAT.fieldOf("health_threshold").forGetter(BossPhase::healthThreshold)
    ).apply(instance, BossPhase::new));

    public BossPhase {
        healthThreshold = Math.clamp(healthThreshold, 0.0F, 1.0F);
    }
}
