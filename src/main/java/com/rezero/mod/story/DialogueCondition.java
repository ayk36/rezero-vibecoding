package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/** Allowlisted, server-evaluated dialogue branch condition. */
public record DialogueCondition(String type, Optional<String> value, int amount) {
    public static final Codec<DialogueCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(DialogueCondition::type),
            Codec.STRING.optionalFieldOf("value").forGetter(DialogueCondition::value),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(DialogueCondition::amount)
    ).apply(instance, DialogueCondition::new));
}
