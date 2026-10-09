package com.rezero.mod.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEventStatusTest {
    @Test
    void exposesAllPersistentEventOutcomes() {
        assertEquals(5, WorldEventStatus.values().length);
        assertEquals(WorldEventStatus.FAILED, WorldEventStatus.valueOf("FAILED"));
    }
}
