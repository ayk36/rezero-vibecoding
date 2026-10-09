package com.rezero.mod.story;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactionReputationTest {
    @Test
    void factionReputationClampsAtBothBounds() {
        var faction = ReZeroMod.id("witch_cult");
        StoryData positive = StoryData.DEFAULT.withFactionReputation(faction, 500);
        StoryData negative = positive.withFactionReputation(faction, -1_000);

        assertEquals(100, positive.factionReputations().get(faction));
        assertEquals(-100, negative.factionReputations().get(faction));
    }
}
