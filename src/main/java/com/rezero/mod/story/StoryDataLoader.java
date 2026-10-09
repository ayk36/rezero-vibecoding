package com.rezero.mod.story;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Explicitly reloads datapack definitions at server start and after `/reload`. */
public final class StoryDataLoader {
    private static Map<Identifier, QuestDefinition> quests = Map.of();
    private static Map<Identifier, DialogueDefinition> dialogues = Map.of();

    private StoryDataLoader() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(StoryDataLoader::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                reload(server);
            }
        });
    }

    public static QuestDefinition quest(Identifier id) {
        return quests.get(id);
    }

    public static DialogueDefinition dialogue(Identifier id) {
        return dialogues.get(id);
    }

    private static void reload(MinecraftServer server) {
        quests = load(server.getResourceManager(), "quests", QuestDefinition.CODEC);
        dialogues = load(server.getResourceManager(), "dialogues", DialogueDefinition.CODEC);
        validateReferences();
        ReZeroMod.LOGGER.info("[ReZero-Story] Loaded {} quests and {} dialogues.", quests.size(), dialogues.size());
    }

    private static void validateReferences() {
        quests.forEach((questId, quest) -> quest.prerequisites().forEach(prerequisite -> {
            if (!quests.containsKey(prerequisite)) {
                ReZeroMod.LOGGER.error("[ReZero-Story] Quest {} references missing prerequisite {}.", questId, prerequisite);
            }
        }));
        dialogues.forEach((dialogueId, dialogue) -> dialogue.nodes().forEach((nodeId, node) -> node.choices().forEach(choice -> {
            choice.next().ifPresent(next -> {
                if (!dialogue.nodes().containsKey(next)) {
                    ReZeroMod.LOGGER.error("[ReZero-Story] Dialogue {} node {} references missing node {}.", dialogueId, nodeId, next);
                }
            });
            choice.action().ifPresent(action -> {
                if (("start_quest".equals(action.type()) || "complete_quest".equals(action.type())) && action.value().isPresent()) {
                    Identifier questId = Identifier.tryParse(action.value().get());
                    if (questId == null || !quests.containsKey(questId)) {
                        ReZeroMod.LOGGER.error("[ReZero-Story] Dialogue {} references missing quest {}.", dialogueId, action.value().get());
                    }
                }
            });
        })));
    }

    private static <T> Map<Identifier, T> load(ResourceManager resources, String directory, com.mojang.serialization.Codec<T> codec) {
        Map<Identifier, T> loaded = new HashMap<>();
        resources.listResources(directory, id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier contentId = toContentId(fileId, directory);
            try (var reader = resource.openAsReader()) {
                codec.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero-Story] Invalid {} {}: {}", directory, fileId, error))
                        .ifPresent(definition -> loaded.put(contentId, definition));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero-Story] Failed to load {} {}.", directory, fileId, exception);
            }
        });
        return Map.copyOf(loaded);
    }

    private static Identifier toContentId(Identifier fileId, String directory) {
        String path = fileId.getPath();
        return Identifier.fromNamespaceAndPath(fileId.getNamespace(), path.substring(directory.length() + 1, path.length() - ".json".length()));
    }
}
