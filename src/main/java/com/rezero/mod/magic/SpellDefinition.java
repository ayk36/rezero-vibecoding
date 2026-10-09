package com.rezero.mod.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Balance data for a spell. Java supplies only the server-side effect behavior. */
public record SpellDefinition(String element, int manaCost, int staminaCost, int cooldownTicks, float power, int range) {
    public static final Codec<SpellDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("element").forGetter(SpellDefinition::element),
            Codec.INT.optionalFieldOf("mana_cost", 0).forGetter(SpellDefinition::manaCost),
            Codec.INT.optionalFieldOf("stamina_cost", 0).forGetter(SpellDefinition::staminaCost),
            Codec.INT.optionalFieldOf("cooldown", 0).forGetter(SpellDefinition::cooldownTicks),
            Codec.FLOAT.optionalFieldOf("power", 0.0F).forGetter(SpellDefinition::power),
            Codec.INT.optionalFieldOf("range", 16).forGetter(SpellDefinition::range)
    ).apply(instance, SpellDefinition::new));

    public SpellDefinition {
        manaCost = Math.clamp(manaCost, 0, 10_000);
        staminaCost = Math.clamp(staminaCost, 0, 10_000);
        cooldownTicks = Math.clamp(cooldownTicks, 0, 20 * 60);
        power = Math.clamp(power, 0.0F, 1_000.0F);
        range = Math.clamp(range, 1, 64);
    }
}
