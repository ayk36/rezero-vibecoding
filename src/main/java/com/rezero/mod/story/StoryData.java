package com.rezero.mod.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

/** Persistent server-owned quest, flag, and relationship state for one player. */
public record StoryData(Map<Identifier, QuestStatus> quests, Map<Identifier, QuestProgress> questProgress, Set<String> flags, Map<Identifier, Integer> relationships, Map<Identifier, Integer> factionReputations) {
    public static final StoryData DEFAULT = new StoryData(Map.of(), Map.of(), Set.of(), Map.of(), Map.of());
    private static final Codec<QuestStatus> QUEST_STATUS_CODEC = Codec.STRING.xmap(QuestStatus::valueOf, QuestStatus::name);
    private static final Codec<Map<Identifier, QuestStatus>> QUESTS_CODEC = Codec.unboundedMap(Identifier.CODEC, QUEST_STATUS_CODEC);
    private static final Codec<Map<Identifier, Integer>> RELATIONSHIPS_CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT);
    private static final Codec<Map<Identifier, QuestProgress>> QUEST_PROGRESS_CODEC = Codec.unboundedMap(Identifier.CODEC, QuestProgress.CODEC);
    private static final Codec<Map<Identifier, Integer>> FACTION_CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT);
    private static final Codec<Set<String>> FLAGS_CODEC = Codec.STRING.listOf().xmap(Set::copyOf, java.util.ArrayList::new);

    public static final Codec<StoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            QUESTS_CODEC.optionalFieldOf("quests", Map.of()).forGetter(StoryData::quests),
            QUEST_PROGRESS_CODEC.optionalFieldOf("quest_progress", Map.of()).forGetter(StoryData::questProgress),
            FLAGS_CODEC.optionalFieldOf("flags", Set.of()).forGetter(StoryData::flags),
            RELATIONSHIPS_CODEC.optionalFieldOf("relationships", Map.of()).forGetter(StoryData::relationships),
            FACTION_CODEC.optionalFieldOf("faction_reputations", Map.of()).forGetter(StoryData::factionReputations)
    ).apply(instance, StoryData::new));

    public StoryData {
        quests = Map.copyOf(quests);
        questProgress = Map.copyOf(questProgress);
        flags = Set.copyOf(flags);
        relationships = Map.copyOf(relationships);
        factionReputations = Map.copyOf(factionReputations);
    }

    public StoryData withQuest(Identifier questId, QuestStatus status) {
        var updated = new java.util.HashMap<>(quests);
        updated.put(questId, status);
        return new StoryData(updated, questProgress, flags, relationships, factionReputations);
    }

    public StoryData withoutQuest(Identifier questId) {
        var statuses = new java.util.HashMap<>(quests);
        var progress = new java.util.HashMap<>(questProgress);
        statuses.remove(questId);
        progress.remove(questId);
        return new StoryData(statuses, progress, flags, relationships, factionReputations);
    }

    public StoryData withQuestProgress(Identifier questId, QuestProgress progress) {
        var updated = new java.util.HashMap<>(questProgress);
        updated.put(questId, progress);
        return new StoryData(quests, updated, flags, relationships, factionReputations);
    }

    public StoryData withFlag(String flag) {
        var updated = new java.util.HashSet<>(flags);
        updated.add(flag);
        return new StoryData(quests, questProgress, updated, relationships, factionReputations);
    }

    public StoryData withRelationship(Identifier npcId, int delta) {
        var updated = new java.util.HashMap<>(relationships);
        updated.put(npcId, Math.clamp(updated.getOrDefault(npcId, 0) + delta, -100, 100));
        return new StoryData(quests, questProgress, flags, updated, factionReputations);
    }

    public StoryData withFactionReputation(Identifier faction, int delta) {
        var updated = new java.util.HashMap<>(factionReputations);
        updated.put(faction, Math.clamp(updated.getOrDefault(faction, 0) + delta, -100, 100));
        return new StoryData(quests, questProgress, flags, relationships, updated);
    }

    /** Memory flags intentionally survive Return by Death checkpoint restoration. */
    public StoryData mergeRetainedMemory(StoryData currentTimeline) {
        var retained = new java.util.HashSet<>(flags);
        currentTimeline.flags().stream().filter(flag -> flag.startsWith("memory.")).forEach(retained::add);
        return new StoryData(quests, questProgress, retained, relationships, factionReputations);
    }
}
