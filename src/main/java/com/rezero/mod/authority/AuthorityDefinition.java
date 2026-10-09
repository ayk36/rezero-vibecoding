package com.rezero.mod.authority;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Data-driven authority metadata. Effects are explicitly allowlisted in Java. */
public record AuthorityDefinition(String name, String behavior, int cooldownTicks, int manaCost) {
    public static final Codec<AuthorityDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(AuthorityDefinition::name),
            Codec.STRING.fieldOf("behavior").forGetter(AuthorityDefinition::behavior),
            Codec.INT.optionalFieldOf("cooldown", 100).forGetter(AuthorityDefinition::cooldownTicks),
            Codec.INT.optionalFieldOf("mana_cost", 0).forGetter(AuthorityDefinition::manaCost)
    ).apply(instance, AuthorityDefinition::new));

    public AuthorityDefinition {
        cooldownTicks = Math.clamp(cooldownTicks, 0, 20 * 60);
        manaCost = Math.clamp(manaCost, 0, 10_000);
    }
}
