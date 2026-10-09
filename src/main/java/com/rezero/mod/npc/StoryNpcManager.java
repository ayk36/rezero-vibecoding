package com.rezero.mod.npc;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.story.StoryDataManager;
import com.rezero.mod.story.StoryManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

/** Enables named vanilla entities as a low-risk first NPC implementation. */
public final class StoryNpcManager {
    private static final String TAG_PREFIX = "rezero_npc:";
    private StoryNpcManager() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            Identifier profileId = profileId(entity);
            if (profileId == null) {
                return InteractionResult.PASS;
            }

            NpcProfile profile = NpcProfileLoader.get(profileId);
            if (profile == null) {
                return InteractionResult.PASS;
            }
            if (!isAvailable(profile, level.getDayTime())) {
                serverPlayer.sendSystemMessage(Component.literal(profile.name() + " is unavailable right now."));
                return InteractionResult.SUCCESS;
            }

            StoryDataManager.INSTANCE.changeRelationship(serverPlayer, profileId, 1);
            Identifier faction = Identifier.tryParse(profile.faction());
            if (faction != null) StoryDataManager.INSTANCE.changeFactionReputation(serverPlayer, faction, 1);
            StoryManager.recordTalk(serverPlayer, profileId);
            if (profile.dialogue().isEmpty() || !StoryManager.beginDialogue(serverPlayer, profile.dialogue().get())) {
                serverPlayer.sendSystemMessage(Component.literal(profile.name() + " has nothing to say right now."));
            }
            return InteractionResult.SUCCESS;
        });
    }

    private static Identifier profileId(Entity entity) {
        for (String tag : entity.getTags()) {
            if (!tag.startsWith(TAG_PREFIX)) continue;
            Identifier id = Identifier.tryParse(tag.substring(TAG_PREFIX.length()));
            if (id != null) return id;
        }
        if (!entity.hasCustomName()) {
            return null;
        }
        String rawName = entity.getCustomName().getString();
        Identifier id = Identifier.tryParse(rawName);
        if (id == null || !id.getNamespace().equals(ReZeroMod.MOD_ID)) {
            return null;
        }
        return id;
    }

    public static void markProfile(Entity entity, Identifier profileId, String displayName) {
        entity.addTag(TAG_PREFIX + profileId);
        entity.setCustomName(Component.literal(displayName));
        entity.setCustomNameVisible(true);
    }

    private static boolean isAvailable(NpcProfile profile, long dayTime) {
        long time = Math.floorMod(dayTime, 24_000L);
        boolean day = time >= 1_000L && time < 13_000L;
        return switch (profile.schedule()) {
            case ALWAYS -> true;
            case DAY -> day;
            case NIGHT -> !day;
        };
    }
}
