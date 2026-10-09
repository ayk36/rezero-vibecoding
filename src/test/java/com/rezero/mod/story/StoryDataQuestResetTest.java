package com.rezero.mod.story;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class StoryDataQuestResetTest {
    @Test
    void removesBothQuestStatusAndProgress() {
        var quest = ReZeroMod.id("arrival");
        StoryData data = StoryData.DEFAULT.withQuest(quest, QuestStatus.ACTIVE).withQuestProgress(quest, new QuestProgress(java.util.List.of(1)));
        StoryData reset = data.withoutQuest(quest);

        assertFalse(reset.quests().containsKey(quest));
        assertFalse(reset.questProgress().containsKey(quest));
    }
}
