package com.rezero.mod.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackedStoryStateMutationTest {
    @Test
    void immutableSnapshotsCanReplaceExplicitKeys() {
        TrackedStoryState before = TrackedStoryState.EMPTY.with("door", "closed");
        TrackedStoryState after = before.with("door", "open");

        assertTrue(before.values().get("door").equals("closed"));
        assertTrue(after.values().get("door").equals("open"));
        assertFalse(after.values().containsKey("chest"));
    }
}
