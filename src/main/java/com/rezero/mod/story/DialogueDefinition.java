package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record DialogueDefinition(String speaker, String startNode, Map<String, DialogueNode> nodes) {
    private static final Codec<Map<String, DialogueNode>> NODE_MAP_CODEC = Codec.unboundedMap(Codec.STRING, DialogueNode.CODEC);
    public static final Codec<DialogueDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("speaker").forGetter(DialogueDefinition::speaker),
            Codec.STRING.optionalFieldOf("start", "start").forGetter(DialogueDefinition::startNode),
            NODE_MAP_CODEC.fieldOf("nodes").forGetter(DialogueDefinition::nodes)
    ).apply(instance, DialogueDefinition::new));

    public DialogueDefinition {
        nodes = Map.copyOf(nodes);
        if (!nodes.containsKey(startNode)) {
            throw new IllegalArgumentException("Dialogue start node is missing: " + startNode);
        }
    }
}
