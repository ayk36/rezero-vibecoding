package com.rezero.mod.story;

import com.rezero.mod.ReZeroMod;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestProgressTest {
    @Test
    void initializesOneProgressValuePerObjective() {
        QuestDefinition definition = new QuestDefinition("Test", "", List.of(), List.of(
                new QuestObjectiveDefinition(QuestObjectiveType.TALK, ReZeroMod.id("emilia"), 1),
                new QuestObjectiveDefinition(QuestObjectiveType.KILL, Identifier.parse("minecraft:zombie"), 3)
        ), 0, java.util.Optional.empty(), 0);

        QuestProgress progress = QuestProgress.emptyFor(definition);

        assertEquals(List.of(0, 0), progress.objectives());
    }

    @Test
    void capsProgressAtTheObjectiveTarget() {
        QuestProgress progress = new QuestProgress(List.of(2));

        assertEquals(3, progress.increment(0, 3).value(0));
        assertEquals(3, progress.increment(0, 3).increment(0, 3).value(0));
    }
}
