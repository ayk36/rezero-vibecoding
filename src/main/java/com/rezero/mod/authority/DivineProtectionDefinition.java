package com.rezero.mod.authority;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Data-driven protection metadata. */
public record DivineProtectionDefinition(String name, String behavior, int strength) {
    public static final Codec<DivineProtectionDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(DivineProtectionDefinition::name),
            Codec.STRING.fieldOf("behavior").forGetter(DivineProtectionDefinition::behavior),
            Codec.INT.optionalFieldOf("strength", 1).forGetter(DivineProtectionDefinition::strength)
    ).apply(instance, DivineProtectionDefinition::new));

    public DivineProtectionDefinition {
        strength = Math.clamp(strength, 1, 10);
    }
}
