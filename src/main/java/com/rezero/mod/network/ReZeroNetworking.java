package com.rezero.mod.network;

import com.rezero.mod.ability.AbilityManager;
import com.rezero.mod.network.payload.CreateCheckpointPayload;
import com.rezero.mod.network.payload.PlayerDataPayload;
import com.rezero.mod.network.payload.ReturnByDeathPayload;
import com.rezero.mod.network.payload.UseAbilityPayload;
import com.rezero.mod.network.payload.QuestSummaryPayload;
import com.rezero.mod.network.payload.MagicSummaryPayload;
import com.rezero.mod.network.payload.OpenDialoguePayload;
import com.rezero.mod.network.payload.ChooseDialoguePayload;
import com.rezero.mod.network.payload.AuthoritySummaryPayload;
import com.rezero.mod.network.payload.UseAuthorityPayload;
import com.rezero.mod.network.payload.RelationshipSummaryPayload;
import com.rezero.mod.network.payload.DodgePayload;
import com.rezero.mod.network.payload.FactionSummaryPayload;
import com.rezero.mod.network.payload.AbilityCooldownPayload;
import com.rezero.mod.player.PlayerData;
import com.rezero.mod.returnbydeath.ReturnByDeathManager;
import com.rezero.mod.story.QuestDefinition;
import com.rezero.mod.story.QuestProgress;
import com.rezero.mod.story.StoryData;
import com.rezero.mod.story.StoryDataLoader;
import com.rezero.mod.magic.MagicData;
import com.rezero.mod.story.StoryManager;
import com.rezero.mod.authority.AuthorityData;
import com.rezero.mod.authority.AuthorityRuntime;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;

/** Common payload registration and authoritative C2S intent handling. */
public final class ReZeroNetworking {
    private ReZeroNetworking() {
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(PlayerDataPayload.TYPE, PlayerDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ReturnByDeathPayload.TYPE, ReturnByDeathPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(QuestSummaryPayload.TYPE, QuestSummaryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MagicSummaryPayload.TYPE, MagicSummaryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDialoguePayload.TYPE, OpenDialoguePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AuthoritySummaryPayload.TYPE, AuthoritySummaryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RelationshipSummaryPayload.TYPE, RelationshipSummaryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FactionSummaryPayload.TYPE, FactionSummaryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AbilityCooldownPayload.TYPE, AbilityCooldownPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateCheckpointPayload.TYPE, CreateCheckpointPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UseAbilityPayload.TYPE, UseAbilityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChooseDialoguePayload.TYPE, ChooseDialoguePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UseAuthorityPayload.TYPE, UseAuthorityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DodgePayload.TYPE, DodgePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CreateCheckpointPayload.TYPE, (payload, context) ->
                context.server().execute(() -> ReturnByDeathManager.createCheckpoint(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.TYPE, (payload, context) ->
                context.server().execute(() -> AbilityManager.INSTANCE.use(context.player(), payload))
        );
        ServerPlayNetworking.registerGlobalReceiver(ChooseDialoguePayload.TYPE, (payload, context) ->
                context.server().execute(() -> StoryManager.choose(context.player(), payload.dialogueId(), payload.nodeId(), payload.choiceIndex()))
        );
        ServerPlayNetworking.registerGlobalReceiver(UseAuthorityPayload.TYPE, (payload, context) -> context.server().execute(() -> {
            var entity = payload.targetEntityId() < 0 ? null : context.player().level().getEntity(payload.targetEntityId());
            AuthorityRuntime.INSTANCE.activate(context.player(), payload.authorityId(), entity instanceof net.minecraft.world.entity.LivingEntity living ? living : null);
        }));
        ServerPlayNetworking.registerGlobalReceiver(DodgePayload.TYPE, (payload, context) ->
                context.server().execute(() -> AbilityManager.INSTANCE.dodge(context.player()))
        );
    }

    public static void sendPlayerData(ServerPlayer player, PlayerData data) {
        ServerPlayNetworking.send(player, PlayerDataPayload.from(data));
    }

    public static void sendReturnByDeath(ServerPlayer player, boolean unlocked, boolean checkpointAvailable) {
        ServerPlayNetworking.send(player, new ReturnByDeathPayload(unlocked, checkpointAvailable, false));
    }

    public static void playReturnByDeathEffect(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ReturnByDeathPayload(true, true, true));
    }

    public static void sendQuestSummary(ServerPlayer player, StoryData data) {
        var entries = new ArrayList<QuestSummaryPayload.Entry>();
        data.quests().forEach((questId, status) -> {
            QuestDefinition definition = StoryDataLoader.quest(questId);
            if (definition == null) {
                return;
            }
            QuestProgress progress = data.questProgress().getOrDefault(questId, QuestProgress.emptyFor(definition));
            int completed = 0;
            for (int index = 0; index < definition.objectives().size(); index++) {
                if (progress.value(index) >= definition.objectives().get(index).count()) {
                    completed++;
                }
            }
            entries.add(new QuestSummaryPayload.Entry(questId, definition.title(), status.name(), completed, definition.objectives().size()));
        });
        ServerPlayNetworking.send(player, new QuestSummaryPayload(entries));
    }

    public static void sendMagicSummary(ServerPlayer player, MagicData data) {
        ServerPlayNetworking.send(player, new MagicSummaryPayload(data.learnedSpells().stream().sorted(java.util.Comparator.comparing(Identifier::toString)).toList()));
    }

    public static void openDialogue(ServerPlayer player, OpenDialoguePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendAuthoritySummary(ServerPlayer player, AuthorityData data) {
        var comparator = java.util.Comparator.comparing(Identifier::toString);
        ServerPlayNetworking.send(player, new AuthoritySummaryPayload(
                data.authorities().stream().sorted(comparator).toList(),
                data.divineProtections().stream().sorted(comparator).toList()
        ));
    }

    public static void sendRelationshipSummary(ServerPlayer player, StoryData data) {
        var entries = data.relationships().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(java.util.Comparator.comparing(Identifier::toString)))
                .map(entry -> new RelationshipSummaryPayload.Entry(entry.getKey(), entry.getValue()))
                .toList();
        ServerPlayNetworking.send(player, new RelationshipSummaryPayload(entries));
    }

    public static void sendFactionSummary(ServerPlayer player, StoryData data) {
        var entries = data.factionReputations().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(java.util.Comparator.comparing(Identifier::toString)))
                .map(entry -> new FactionSummaryPayload.Entry(entry.getKey(), entry.getValue()))
                .toList();
        ServerPlayNetworking.send(player, new FactionSummaryPayload(entries));
    }

    public static void sendAbilityCooldown(ServerPlayer player, Identifier abilityId, long readyAtGameTime) {
        ServerPlayNetworking.send(player, new AbilityCooldownPayload(abilityId, readyAtGameTime));
    }
}
