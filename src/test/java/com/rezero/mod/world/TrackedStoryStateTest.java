package com.rezero.mod.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackedStoryStateTest {
    @Test
    void storesOnlyExplicitTrackedValues() {
        TrackedStoryState state = TrackedStoryState.EMPTY.with("mansion_door", "open");

        assertEquals("open", state.values().get("mansion_door"));
        assertEquals(1, state.values().size());
    }
}
