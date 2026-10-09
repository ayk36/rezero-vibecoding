package com.rezero.mod.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.rezero.mod.story.StoryDataManager;
import com.rezero.mod.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;

/** Small persisted state machine for world-scale boss/story events. */
public final class WorldEventManager extends SavedData {
    private static final String DATA_ID = "rezero_world_events";
    private static final com.mojang.serialization.Codec<WorldEventManager> CODEC = WorldEventData.CODEC.xmap(
            state -> {
                WorldEventManager data = new WorldEventManager();
                data.data = state;
                return data;
            }, data -> data.data
    );
    private static final SavedDataType<WorldEventManager> TYPE = new SavedDataType<>(DATA_ID, WorldEventManager::new, CODEC, null);
    private WorldEventData data = WorldEventData.DEFAULT;

    public static WorldEventManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public WorldEventStatus status(Identifier eventId) {
        return data.statuses().getOrDefault(eventId, WorldEventStatus.INACTIVE);
    }

    public boolean transition(Identifier eventId, WorldEventStatus expected, WorldEventStatus next) {
        if (status(eventId) != expected) {
            return false;
        }
        data = data.with(eventId, next);
        setDirty();
        return true;
    }

    public void set(Identifier eventId, WorldEventStatus status) {
        data = data.with(eventId, status);
        setDirty();
    }

    public void complete(ServerLevel level, Identifier eventId) {
        if (status(eventId) == WorldEventStatus.COMPLETED) return;
        set(eventId, WorldEventStatus.COMPLETED);
        for (ServerPlayer player : level.players()) {
            StoryDataManager.INSTANCE.setFlag(player, eventId.getPath() + "_defeated");
            player.getInventory().add(new ItemStack(ModItems.LUGUNICA_GOLD_COIN, 2));
        }
    }

    public void fail(Identifier eventId) {
        if (status(eventId) == WorldEventStatus.ACTIVE) set(eventId, WorldEventStatus.FAILED);
    }

    public void failAndCleanup(ServerLevel level, Identifier eventId) {
        fail(eventId);
        for (Entity entity : level.getAllEntities()) {
            if (entity.getTags().contains("rezero_event:" + eventId)) entity.discard();
        }
    }
}
