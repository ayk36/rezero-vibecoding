package com.rezero.mod.world;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEventDefinitionTest {
    @Test
    void retainsConfiguredBossAndStoryGate() {
        WorldEventDefinition definition = new WorldEventDefinition(ReZeroMod.id("white_whale"), "mansion_arc_complete");

        assertEquals(ReZeroMod.id("white_whale"), definition.boss());
        assertEquals("mansion_arc_complete", definition.requiredStoryFlag());
    }
}
