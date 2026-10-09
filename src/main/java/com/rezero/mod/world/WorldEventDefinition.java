package com.rezero.mod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Datapack declaration for a persistent world event and its boss endpoint. */
public record WorldEventDefinition(Identifier boss, String requiredStoryFlag) {
    public static final Codec<WorldEventDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("boss").forGetter(WorldEventDefinition::boss),
            Codec.STRING.optionalFieldOf("required_story_flag", "").forGetter(WorldEventDefinition::requiredStoryFlag)
    ).apply(instance, WorldEventDefinition::new));
}
