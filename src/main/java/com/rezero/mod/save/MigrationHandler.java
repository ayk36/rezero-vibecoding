package com.rezero.mod.save;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.player.PlayerData;
import com.rezero.mod.story.StoryData;

/** Central future migration entry point. v1 data is already current. */
public final class MigrationHandler {
    private MigrationHandler() {
    }

    public static void initialize() {
        ReZeroMod.LOGGER.info("[ReZero] Persistent data schema v{} initialized.", DataVersion.CURRENT);
    }

    public static PlayerData migratePlayerData(PlayerData data) {
        // Record constructors already clamp values, keeping v1 save data safe under future field changes.
        return new PlayerData(data.level(), data.experience(), data.mana(), data.maxMana(), data.stamina(), data.maxStamina(),
                data.strength(), data.defense(), data.magicPower(), data.magicResistance(), data.speed(), data.luck());
    }

    public static StoryData migrateStoryData(StoryData data) {
        // v1-to-v2 added faction reputation; the codec supplies an empty map for old worlds.
        return new StoryData(data.quests(), data.questProgress(), data.flags(), data.relationships(), data.factionReputations());
    }
}
