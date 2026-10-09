package com.rezero.mod.magic;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MagicMemoryTest {
    @Test
    void preservesNewlyLearnedSpellKnowledge() {
        MagicData checkpoint = new MagicData(java.util.Set.of(ReZeroMod.id("shadow_bind")));
        MagicData timeline = new MagicData(java.util.Set.of(ReZeroMod.id("minya")));

        MagicData restored = checkpoint.mergeKnowledge(timeline);

        assertTrue(restored.learnedSpells().contains(ReZeroMod.id("shadow_bind")));
        assertTrue(restored.learnedSpells().contains(ReZeroMod.id("minya")));
    }
}
