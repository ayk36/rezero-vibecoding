package com.rezero.mod.story;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryRetentionTest {
    @Test
    void retainsOnlyMemoryPrefixedFlagsAcrossRestoration() {
        StoryData checkpoint = StoryData.DEFAULT.withFlag("ordinary_flag");
        StoryData timeline = StoryData.DEFAULT.withFlag("memory.first_return").withFlag("temporary_flag");

        StoryData restored = checkpoint.mergeRetainedMemory(timeline);

        assertTrue(restored.flags().contains("ordinary_flag"));
        assertTrue(restored.flags().contains("memory.first_return"));
        assertFalse(restored.flags().contains("temporary_flag"));
    }
}
