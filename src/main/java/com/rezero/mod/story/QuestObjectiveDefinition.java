package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** A target and count. Target means entity type for KILL and NPC profile for TALK. */
public record QuestObjectiveDefinition(QuestObjectiveType type, Identifier target, int count) {
    private static final Codec<QuestObjectiveType> TYPE_CODEC = Codec.STRING.xmap(
            value -> QuestObjectiveType.valueOf(value.toUpperCase(java.util.Locale.ROOT)),
            value -> value.name().toLowerCase(java.util.Locale.ROOT)
    );
    public static final Codec<QuestObjectiveDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TYPE_CODEC.fieldOf("type").forGetter(QuestObjectiveDefinition::type),
            Identifier.CODEC.fieldOf("target").forGetter(QuestObjectiveDefinition::target),
            Codec.INT.optionalFieldOf("count", 1).forGetter(QuestObjectiveDefinition::count)
    ).apply(instance, QuestObjectiveDefinition::new));

    public QuestObjectiveDefinition {
        count = Math.clamp(count, 1, 10_000);
    }
}
