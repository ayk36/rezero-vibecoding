package com.rezero.mod.story;

import com.rezero.mod.ReZeroMod;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoryDataTest {
    @Test
    void relationshipValuesStayWithinConfiguredBounds() {
        Identifier emilia = ReZeroMod.id("emilia");

        StoryData friendly = StoryData.DEFAULT.withRelationship(emilia, 200);
        StoryData hostile = friendly.withRelationship(emilia, -500);

        assertEquals(100, friendly.relationships().get(emilia));
        assertEquals(-100, hostile.relationships().get(emilia));
    }
}
