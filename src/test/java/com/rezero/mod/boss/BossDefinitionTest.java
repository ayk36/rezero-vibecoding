package com.rezero.mod.boss;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossDefinitionTest {
    @Test
    void clampsConfiguredBossHealth() {
        BossDefinition definition = new BossDefinition("Test", ReZeroMod.id("test"), 100_000.0F, -1, -1, List.of(new BossPhase("start", 1.0F)));

        assertEquals(10_000.0F, definition.maxHealth());
        assertEquals(0, definition.experienceReward());
        assertEquals(0, definition.goldReward());
    }
}
