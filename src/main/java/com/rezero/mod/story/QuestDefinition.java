package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Validated data-driven quest metadata and ordered, server-evaluated objectives. */
public record QuestDefinition(String title, String description, List<Identifier> prerequisites, List<QuestObjectiveDefinition> objectives, int experienceReward, Optional<Identifier> factionReward, int factionReputationReward) {
    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").forGetter(QuestDefinition::title),
            Codec.STRING.fieldOf("description").forGetter(QuestDefinition::description),
            Identifier.CODEC.listOf().optionalFieldOf("prerequisites", List.of()).forGetter(QuestDefinition::prerequisites),
            QuestObjectiveDefinition.CODEC.listOf().optionalFieldOf("objectives", List.of()).forGetter(QuestDefinition::objectives),
            Codec.INT.optionalFieldOf("experience_reward", 0).forGetter(QuestDefinition::experienceReward),
            Identifier.CODEC.optionalFieldOf("faction_reward").forGetter(QuestDefinition::factionReward),
            Codec.INT.optionalFieldOf("faction_reputation_reward", 0).forGetter(QuestDefinition::factionReputationReward)
    ).apply(instance, QuestDefinition::new));

    public QuestDefinition {
        objectives = List.copyOf(objectives);
        prerequisites = List.copyOf(prerequisites);
        experienceReward = Math.clamp(experienceReward, 0, 100_000);
        factionReputationReward = Math.clamp(factionReputationReward, -100, 100);
    }
}
