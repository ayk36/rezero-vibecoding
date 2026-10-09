package com.rezero.mod.world;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEventDataTest {
    @Test
    void storesEventStateByStableIdentifier() {
        var eventId = ReZeroMod.id("white_whale_hunt");
        WorldEventData data = WorldEventData.DEFAULT.with(eventId, WorldEventStatus.AVAILABLE);

        assertEquals(WorldEventStatus.AVAILABLE, data.statuses().get(eventId));
    }
}
