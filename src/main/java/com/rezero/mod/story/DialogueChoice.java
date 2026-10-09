package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.List;

public record DialogueChoice(String text, Optional<String> next, Optional<DialogueAction> action, List<DialogueCondition> conditions) {
    public static final Codec<DialogueChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(DialogueChoice::text),
            Codec.STRING.optionalFieldOf("next").forGetter(DialogueChoice::next),
            DialogueAction.CODEC.optionalFieldOf("action").forGetter(DialogueChoice::action),
            DialogueCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(DialogueChoice::conditions)
    ).apply(instance, DialogueChoice::new));

    public DialogueChoice {
        conditions = List.copyOf(conditions);
    }
}
