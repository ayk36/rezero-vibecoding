package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/** Allowlisted dialogue actions. Unsupported actions cannot be loaded or executed. */
public record DialogueAction(String type, Optional<String> value, int amount) {
    public static final Codec<DialogueAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(DialogueAction::type),
            Codec.STRING.optionalFieldOf("value").forGetter(DialogueAction::value),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(DialogueAction::amount)
    ).apply(instance, DialogueAction::new));
}
