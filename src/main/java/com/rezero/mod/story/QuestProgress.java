package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** Persistent progress matching the definition objective order. */
public record QuestProgress(List<Integer> objectives) {
    public static final Codec<QuestProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf().optionalFieldOf("objectives", List.of()).forGetter(QuestProgress::objectives)
    ).apply(instance, QuestProgress::new));

    public QuestProgress {
        objectives = objectives.stream().map(value -> Math.max(0, value)).toList();
    }

    public static QuestProgress emptyFor(QuestDefinition definition) {
        return new QuestProgress(java.util.Collections.nCopies(definition.objectives().size(), 0));
    }

    public int value(int objectiveIndex) {
        return objectiveIndex >= 0 && objectiveIndex < objectives.size() ? objectives.get(objectiveIndex) : 0;
    }

    public QuestProgress increment(int objectiveIndex, int max) {
        if (objectiveIndex < 0 || objectiveIndex >= objectives.size()) {
            return this;
        }
        var updated = new java.util.ArrayList<>(objectives);
        updated.set(objectiveIndex, Math.min(max, updated.get(objectiveIndex) + 1));
        return new QuestProgress(updated);
    }
}
