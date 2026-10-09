package com.rezero.mod.client;

import com.rezero.mod.network.payload.PlayerDataPayload;
import com.rezero.mod.network.payload.ReturnByDeathPayload;
import com.rezero.mod.network.payload.QuestSummaryPayload;
import java.util.List;
import com.rezero.mod.network.payload.MagicSummaryPayload;
import com.rezero.mod.network.payload.AuthoritySummaryPayload;
import com.rezero.mod.network.payload.RelationshipSummaryPayload;
import com.rezero.mod.network.payload.FactionSummaryPayload;
import com.rezero.mod.network.payload.AbilityCooldownPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Render/UI snapshot. This cache has no gameplay authority. */
public final class ClientReZeroState {
    private static PlayerDataPayload playerData;
    private static ReturnByDeathPayload returnByDeath;
    private static List<QuestSummaryPayload.Entry> quests = List.of();
    private static List<net.minecraft.resources.Identifier> learnedSpells = List.of();
    private static List<net.minecraft.resources.Identifier> authorities = List.of();
    private static List<net.minecraft.resources.Identifier> protections = List.of();
    private static List<RelationshipSummaryPayload.Entry> relationships = List.of();
    private static List<FactionSummaryPayload.Entry> factions = List.of();
    private static final java.util.Map<net.minecraft.resources.Identifier, Long> cooldowns = new java.util.HashMap<>();
    private static long returnByDeathTransitionStartedAt = -1L;

    private ClientReZeroState() {
    }

    public static void setPlayerData(PlayerDataPayload data) {
        playerData = data;
    }

    public static void setReturnByDeath(ReturnByDeathPayload data) {
        returnByDeath = data;
        if (data.restoring()) {
            returnByDeathTransitionStartedAt = System.nanoTime();
        }
    }

    public static PlayerDataPayload playerData() {
        return playerData;
    }

    public static ReturnByDeathPayload returnByDeath() {
        return returnByDeath;
    }

    public static float returnByDeathFade() {
        if (returnByDeathTransitionStartedAt < 0L) return 0.0F;
        float progress = Math.min(1.0F, (System.nanoTime() - returnByDeathTransitionStartedAt) / 1_500_000_000.0F);
        if (progress >= 1.0F) returnByDeathTransitionStartedAt = -1L;
        return progress < 0.5F ? progress * 2.0F : (1.0F - progress) * 2.0F;
    }

    public static void setQuests(QuestSummaryPayload data) {
        quests = data.quests();
    }

    public static List<QuestSummaryPayload.Entry> quests() {
        return quests;
    }

    public static void setMagic(MagicSummaryPayload data) {
        learnedSpells = data.learnedSpells();
    }

    public static List<net.minecraft.resources.Identifier> learnedSpells() {
        return learnedSpells;
    }

    public static void setAuthority(AuthoritySummaryPayload data) {
        authorities = data.authorities();
        protections = data.protections();
    }

    public static List<net.minecraft.resources.Identifier> authorities() { return authorities; }
    public static List<net.minecraft.resources.Identifier> protections() { return protections; }
    public static void setRelationships(RelationshipSummaryPayload data) { relationships = data.entries(); }
    public static List<RelationshipSummaryPayload.Entry> relationships() { return relationships; }
    public static void setFactions(FactionSummaryPayload data) { factions = data.entries(); }
    public static List<FactionSummaryPayload.Entry> factions() { return factions; }
    public static void setCooldown(AbilityCooldownPayload payload) { cooldowns.put(payload.abilityId(), payload.readyAtGameTime()); }
    public static long cooldownReadyAt(net.minecraft.resources.Identifier ability) { return cooldowns.getOrDefault(ability, 0L); }

    public static void notifyUnavailableScreen(Minecraft client, String screen) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal(screen + " UI is planned for a later MVP slice."), true);
        }
    }
}
