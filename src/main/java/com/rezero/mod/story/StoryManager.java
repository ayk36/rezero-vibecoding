package com.rezero.mod.story;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.network.payload.OpenDialoguePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.rezero.mod.player.PlayerDataManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import com.rezero.mod.boss.BossManager;
import com.rezero.mod.world.WorldEventManager;
import com.rezero.mod.world.WorldEventStatus;
import com.rezero.mod.magic.MagicDataManager;
import com.rezero.mod.authority.AuthorityManager;
import com.rezero.mod.returnbydeath.ReturnByDeathManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative dialogue and quest operations. */
public final class StoryManager {
    private static final Map<UUID, DialogueSession> DIALOGUE_SESSIONS = new HashMap<>();
    private StoryManager() {
    }

    public static boolean beginDialogue(ServerPlayer player, Identifier dialogueId) {
        DialogueDefinition dialogue = StoryDataLoader.dialogue(dialogueId);
        if (dialogue == null) {
            ReZeroMod.LOGGER.warn("[ReZero-Story] Dialogue {} is unavailable.", dialogueId);
            return false;
        }
        DIALOGUE_SESSIONS.put(player.getUUID(), new DialogueSession(dialogueId, dialogue.startNode()));
        return presentNode(player, dialogueId, dialogue.startNode());
    }

    public static boolean choose(ServerPlayer player, Identifier dialogueId, String nodeId, int choiceIndex) {
        DialogueDefinition dialogue = StoryDataLoader.dialogue(dialogueId);
        DialogueSession session = DIALOGUE_SESSIONS.get(player.getUUID());
        if (dialogue == null || choiceIndex < 0 || session == null || !session.dialogueId().equals(dialogueId) || !session.nodeId().equals(nodeId)) {
            return false;
        }

        DialogueNode node = dialogue.nodes().get(nodeId);
        if (node == null || choiceIndex >= node.choices().size()) {
            return false;
        }

        DialogueChoice choice = node.choices().get(choiceIndex);
        if (!conditionsMet(player, choice.conditions())) {
            return false;
        }
        choice.action().ifPresent(action -> executeAction(player, action));
        return choice.next().map(next -> {
            DIALOGUE_SESSIONS.put(player.getUUID(), new DialogueSession(dialogueId, next));
            return presentNode(player, dialogueId, next);
        }).orElseGet(() -> {
            DIALOGUE_SESSIONS.remove(player.getUUID());
            return true;
        });
    }

    public static boolean startQuest(ServerPlayer player, Identifier questId) {
        QuestDefinition quest = StoryDataLoader.quest(questId);
        if (quest == null || !prerequisitesComplete(player, quest) || !StoryDataManager.INSTANCE.startQuest(player, questId, quest)) {
            return false;
        }
        player.sendSystemMessage(Component.literal("Quest started: " + quest.title()));
        if (quest.objectives().isEmpty()) {
            completeQuest(player, questId);
        }
        return true;
    }

    public static boolean completeQuest(ServerPlayer player, Identifier questId) {
        QuestDefinition quest = StoryDataLoader.quest(questId);
        if (quest == null || !StoryDataManager.INSTANCE.completeQuest(player, questId)) {
            return false;
        }
        if (quest.experienceReward() > 0) {
            PlayerDataManager.INSTANCE.grantExperience(player, quest.experienceReward());
        }
        quest.factionReward().ifPresent(faction -> StoryDataManager.INSTANCE.changeFactionReputation(player, faction, quest.factionReputationReward()));
        StoryDataManager.INSTANCE.setFlag(player, "quest." + questId.getPath() + ".completed");
        player.sendSystemMessage(Component.literal("Quest completed: " + quest.title()));
        return true;
    }

    private static boolean prerequisitesComplete(ServerPlayer player, QuestDefinition quest) {
        StoryData data = StoryDataManager.INSTANCE.get(player);
        return quest.prerequisites().stream().allMatch(id -> data.quests().get(id) == QuestStatus.COMPLETED);
    }

    public static void recordKill(ServerPlayer player, Identifier killedEntityType) {
        recordObjective(player, QuestObjectiveType.KILL, killedEntityType);
    }

    public static void recordTalk(ServerPlayer player, Identifier npcProfile) {
        recordObjective(player, QuestObjectiveType.TALK, npcProfile);
    }

    public static void recordAbilityUse(ServerPlayer player, Identifier abilityId) {
        recordObjective(player, QuestObjectiveType.USE_ABILITY, abilityId);
    }

    public static void recordLocationReach(ServerPlayer player, Identifier locationId) {
        recordObjective(player, QuestObjectiveType.REACH_LOCATION, locationId);
    }

    public static void recordCollect(ServerPlayer player, Identifier itemId, int count) {
        recordObjective(player, QuestObjectiveType.COLLECT, itemId, count);
    }

    public static void recordSurvival(ServerPlayer player) {
        recordObjective(player, QuestObjectiveType.SURVIVE, ReZeroMod.id("survive"));
    }

    public static void recordBossDefeat(ServerPlayer player, Identifier bossId) {
        recordObjective(player, QuestObjectiveType.BOSS, bossId);
    }

    private static void recordObjective(ServerPlayer player, QuestObjectiveType eventType, Identifier target) {
        recordObjective(player, eventType, target, 1);
    }

    private static void recordObjective(ServerPlayer player, QuestObjectiveType eventType, Identifier target, int increment) {
        StoryData data = StoryDataManager.INSTANCE.get(player);
        for (var entry : data.quests().entrySet()) {
            if (entry.getValue() != QuestStatus.ACTIVE) {
                continue;
            }

            Identifier questId = entry.getKey();
            QuestDefinition definition = StoryDataLoader.quest(questId);
            if (definition == null) {
                continue;
            }

            QuestProgress progress = StoryDataManager.INSTANCE.progress(player, questId, definition);
            boolean changed = false;
            for (int index = 0; index < definition.objectives().size(); index++) {
                QuestObjectiveDefinition objective = definition.objectives().get(index);
                if (objective.type() != eventType || !objective.target().equals(target) || progress.value(index) >= objective.count()) {
                    continue;
                }
                progress = increment(progress, index, objective.count(), increment);
                changed = true;
            }

            if (!changed) {
                continue;
            }

            StoryDataManager.INSTANCE.setProgress(player, questId, progress);
            if (isComplete(definition, progress)) {
                completeQuest(player, questId);
            }
        }
    }

    private static QuestProgress increment(QuestProgress progress, int index, int max, int amount) {
        QuestProgress result = progress;
        for (int step = 0; step < amount && result.value(index) < max; step++) result = result.increment(index, max);
        return result;
    }

    private static boolean isComplete(QuestDefinition definition, QuestProgress progress) {
        for (int index = 0; index < definition.objectives().size(); index++) {
            if (progress.value(index) < definition.objectives().get(index).count()) {
                return false;
            }
        }
        return true;
    }

    private static boolean presentNode(ServerPlayer player, Identifier dialogueId, String nodeId) {
        DialogueDefinition dialogue = StoryDataLoader.dialogue(dialogueId);
        if (dialogue == null) {
            return false;
        }
        DialogueNode node = dialogue.nodes().get(nodeId);
        if (node == null) {
            ReZeroMod.LOGGER.warn("[ReZero-Story] Dialogue {} has no node {}.", dialogueId, nodeId);
            return false;
        }

        player.sendSystemMessage(Component.literal(dialogue.speaker() + ": " + node.text()));
        var visibleChoices = new java.util.ArrayList<OpenDialoguePayload.Choice>();
        for (int index = 0; index < node.choices().size(); index++) {
            DialogueChoice choice = node.choices().get(index);
            if (!conditionsMet(player, choice.conditions())) continue;
            player.sendSystemMessage(Component.literal("[" + index + "] " + choice.text()));
            visibleChoices.add(new OpenDialoguePayload.Choice(index, choice.text()));
        }
        ReZeroNetworking.openDialogue(player, new OpenDialoguePayload(
                dialogueId,
                nodeId,
                dialogue.speaker(),
                node.text(),
                visibleChoices
        ));
        return true;
    }

    private static void executeAction(ServerPlayer player, DialogueAction action) {
        switch (action.type()) {
            case "start_quest" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull).ifPresent(quest -> startQuest(player, quest));
            case "complete_quest" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull).ifPresent(quest -> completeQuest(player, quest));
            case "set_flag" -> action.value().ifPresent(flag -> StoryDataManager.INSTANCE.setFlag(player, flag));
            case "change_relationship" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .ifPresent(npc -> StoryDataManager.INSTANCE.changeRelationship(player, npc, action.amount()));
            case "give_item" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .map(BuiltInRegistries.ITEM::getOptional)
                    .ifPresent(item -> item.ifPresent(value -> player.getInventory().add(new ItemStack(value, Math.max(1, action.amount())))));
            case "teleport_landmark" -> action.value().ifPresent(landmark -> teleportToLandmark(player, landmark));
            case "start_boss" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .ifPresent(boss -> BossManager.spawn((net.minecraft.server.level.ServerLevel) player.level(), boss, player.position()));
            case "set_world_event" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .ifPresent(event -> WorldEventManager.get((net.minecraft.server.level.ServerLevel) player.level()).set(event, worldEventStatus(action.amount())));
            case "learn_spell" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .ifPresent(spell -> MagicDataManager.INSTANCE.learn(player, spell));
            case "grant_authority" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .ifPresent(authority -> AuthorityManager.INSTANCE.grantAuthority(player, authority));
            case "grant_divine_protection" -> action.value().map(Identifier::tryParse).filter(java.util.Objects::nonNull)
                    .ifPresent(protection -> AuthorityManager.INSTANCE.grantProtection(player, protection));
            case "retain_memory" -> action.value().ifPresent(flag -> StoryDataManager.INSTANCE.setFlag(player, "memory." + flag));
            case "create_checkpoint" -> ReturnByDeathManager.createCheckpoint(player);
            default -> ReZeroMod.LOGGER.warn("[ReZero-Story] Ignoring unsupported dialogue action {}.", action.type());
        }
    }

    private static WorldEventStatus worldEventStatus(int ordinal) {
        WorldEventStatus[] values = WorldEventStatus.values();
        return values[Math.clamp(ordinal, 0, values.length - 1)];
    }

    private static void teleportToLandmark(ServerPlayer player, String landmark) {
        var values = com.rezero.mod.world.TrackedStoryStateManager.get((net.minecraft.server.level.ServerLevel) player.level()).snapshot().values();
        try {
            int x = Integer.parseInt(values.get(landmark + ".x"));
            int y = Integer.parseInt(values.get(landmark + ".y"));
            int z = Integer.parseInt(values.get(landmark + ".z"));
            player.teleportTo((net.minecraft.server.level.ServerLevel) player.level(), x + 0.5D, y + 1.0D, z + 0.5D, java.util.Set.of(), player.getYRot(), player.getXRot(), false);
        } catch (RuntimeException exception) {
            ReZeroMod.LOGGER.warn("[ReZero-Story] Landmark {} is unavailable for teleport.", landmark);
        }
    }

    private static boolean conditionsMet(ServerPlayer player, java.util.List<DialogueCondition> conditions) {
        StoryData data = StoryDataManager.INSTANCE.get(player);
        for (DialogueCondition condition : conditions) {
            String value = condition.value().orElse("");
            boolean valid = switch (condition.type()) {
                case "story_flag" -> data.flags().contains(value);
                case "quest_active" -> identifier(value).map(id -> data.quests().get(id) == QuestStatus.ACTIVE).orElse(false);
                case "quest_completed" -> identifier(value).map(id -> data.quests().get(id) == QuestStatus.COMPLETED).orElse(false);
                case "player_level" -> PlayerDataManager.INSTANCE.getPlayerData(player).level() >= condition.amount();
                case "relationship" -> identifier(value).map(id -> data.relationships().getOrDefault(id, 0) >= condition.amount()).orElse(false);
                default -> false;
            };
            if (!valid) return false;
        }
        return true;
    }

    private static java.util.Optional<Identifier> identifier(String value) {
        return java.util.Optional.ofNullable(Identifier.tryParse(value));
    }

    private record DialogueSession(Identifier dialogueId, String nodeId) {
    }
}
