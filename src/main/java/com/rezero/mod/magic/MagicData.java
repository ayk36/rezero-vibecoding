package com.rezero.mod.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rezero.mod.ReZeroMod;
import net.minecraft.resources.Identifier;

import java.util.Set;

/** Persistent server-owned spell knowledge. Spell definitions remain datapack content. */
public record MagicData(Set<Identifier> learnedSpells) {
    public static final MagicData DEFAULT = new MagicData(Set.of(ReZeroMod.id("shadow_bind")));
    private static final Codec<Set<Identifier>> SPELL_SET_CODEC = Identifier.CODEC.listOf().xmap(Set::copyOf, java.util.ArrayList::new);
    public static final Codec<MagicData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SPELL_SET_CODEC.optionalFieldOf("learned_spells", DEFAULT.learnedSpells()).forGetter(MagicData::learnedSpells)
    ).apply(instance, MagicData::new));

    public MagicData {
        learnedSpells = Set.copyOf(learnedSpells);
    }

    public MagicData learn(Identifier spellId) {
        var updated = new java.util.HashSet<>(learnedSpells);
        updated.add(spellId);
        return new MagicData(updated);
    }

    /** Learned spell knowledge is retained across Return by Death. */
    public MagicData mergeKnowledge(MagicData currentTimeline) {
        var updated = new java.util.HashSet<>(learnedSpells);
        updated.addAll(currentTimeline.learnedSpells());
        return new MagicData(updated);
    }
}
