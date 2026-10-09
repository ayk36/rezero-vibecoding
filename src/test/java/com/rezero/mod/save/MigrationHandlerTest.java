package com.rezero.mod.save;

import com.rezero.mod.player.PlayerData;
import com.rezero.mod.story.StoryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationHandlerTest {
    @Test
    void keepsPlayerDataWithinPersistentBounds() {
        PlayerData migrated = MigrationHandler.migratePlayerData(new PlayerData(0, -1, 999, 100, -1, 50, 0, 0, 0, 0, 0, 0));

        assertEquals(1, migrated.level());
        assertEquals(100, migrated.mana());
    }

    @Test
    void preservesStoryDataWithFactionMap() {
        StoryData migrated = MigrationHandler.migrateStoryData(StoryData.DEFAULT);

        assertTrue(migrated.factionReputations().isEmpty());
    }
}
