package com.rezero.mod.story;

import com.rezero.mod.registry.ModAttachments;
import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.save.MigrationHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** The only mutable API for player story state. */
public final class StoryDataManager {
    public static final StoryDataManager INSTANCE = new StoryDataManager();

    private StoryDataManager() {
    }

    public StoryData get(ServerPlayer player) {
        return MigrationHandler.migrateStoryData(player.getAttachedOrCreate(ModAttachments.STORY_DATA, () -> StoryData.DEFAULT));
    }

    public void set(ServerPlayer player, StoryData data) {
        player.setAttached(ModAttachments.STORY_DATA, data);
        ReZeroNetworking.sendQuestSummary(player, data);
        ReZeroNetworking.sendRelationshipSummary(player, data);
        ReZeroNetworking.sendFactionSummary(player, data);
    }

    public boolean startQuest(ServerPlayer player, Identifier questId, QuestDefinition definition) {
        StoryData data = get(player);
        if (data.quests().containsKey(questId)) {
            return false;
        }
        set(player, data.withQuest(questId, QuestStatus.ACTIVE).withQuestProgress(questId, QuestProgress.emptyFor(definition)));
        return true;
    }

    public boolean completeQuest(ServerPlayer player, Identifier questId) {
        StoryData data = get(player);
        if (data.quests().get(questId) != QuestStatus.ACTIVE) {
            return false;
        }
        set(player, data.withQuest(questId, QuestStatus.COMPLETED));
        return true;
    }

    public boolean resetQuest(ServerPlayer player, Identifier questId) {
        StoryData data = get(player);
        if (!data.quests().containsKey(questId)) return false;
        set(player, data.withoutQuest(questId));
        return true;
    }

    public void setFlag(ServerPlayer player, String flag) {
        set(player, get(player).withFlag(flag));
    }

    public void changeRelationship(ServerPlayer player, Identifier npcId, int delta) {
        set(player, get(player).withRelationship(npcId, delta));
    }

    public void changeFactionReputation(ServerPlayer player, Identifier faction, int delta) {
        set(player, get(player).withFactionReputation(faction, delta));
    }

    public QuestProgress progress(ServerPlayer player, Identifier questId, QuestDefinition definition) {
        return get(player).questProgress().getOrDefault(questId, QuestProgress.emptyFor(definition));
    }

    public void setProgress(ServerPlayer player, Identifier questId, QuestProgress progress) {
        set(player, get(player).withQuestProgress(questId, progress));
    }
}
