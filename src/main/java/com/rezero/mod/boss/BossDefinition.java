package com.rezero.mod.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Data-driven boss profile. Entity choice and rewards are explicit and server controlled. */
public record BossDefinition(String name, Identifier entityType, float maxHealth, int experienceReward, int goldReward, List<BossPhase> phases) {
    public static final Codec<BossDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(BossDefinition::name),
            Identifier.CODEC.fieldOf("entity_type").forGetter(BossDefinition::entityType),
            Codec.FLOAT.optionalFieldOf("max_health", 100.0F).forGetter(BossDefinition::maxHealth),
            Codec.INT.optionalFieldOf("experience_reward", 0).forGetter(BossDefinition::experienceReward),
            Codec.INT.optionalFieldOf("gold_reward", 0).forGetter(BossDefinition::goldReward),
            BossPhase.CODEC.listOf().fieldOf("phases").forGetter(BossDefinition::phases)
    ).apply(instance, BossDefinition::new));

    public BossDefinition {
        maxHealth = Math.clamp(maxHealth, 1.0F, 10_000.0F);
        experienceReward = Math.clamp(experienceReward, 0, 100_000);
        goldReward = Math.clamp(goldReward, 0, 64);
        phases = List.copyOf(phases);
        if (phases.isEmpty()) {
            throw new IllegalArgumentException("Boss requires at least one phase");
        }
    }
}
