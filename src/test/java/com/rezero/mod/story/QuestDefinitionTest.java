package com.rezero.mod.story;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestDefinitionTest {
    @Test
    void retainsPrerequisitesAndFactionReward() {
        QuestDefinition definition = new QuestDefinition("Test", "", List.of(ReZeroMod.id("arrival")), List.of(), 5,
                Optional.of(ReZeroMod.id("emilia_camp")), 10);

        assertEquals(ReZeroMod.id("arrival"), definition.prerequisites().getFirst());
        assertEquals(10, definition.factionReputationReward());
    }
}
