package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record DialogueNode(String text, List<DialogueChoice> choices) {
    public static final Codec<DialogueNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(DialogueNode::text),
            DialogueChoice.CODEC.listOf().optionalFieldOf("choices", List.of()).forGetter(DialogueNode::choices)
    ).apply(instance, DialogueNode::new));

    public DialogueNode {
        choices = List.copyOf(choices);
    }
}
