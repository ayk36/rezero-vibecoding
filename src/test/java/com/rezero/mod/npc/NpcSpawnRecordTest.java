package com.rezero.mod.npc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NpcSpawnRecordTest {
    @Test
    void retainsDimensionAndPlacement() {
        NpcSpawnRecord record = new NpcSpawnRecord("minecraft:overworld", 1.5D, 64.0D, -3.5D, 90.0F, 0.0F);

        assertEquals("minecraft:overworld", record.dimension());
        assertEquals(1.5D, record.x());
    }
}
