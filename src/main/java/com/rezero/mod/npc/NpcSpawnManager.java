package com.rezero.mod.npc;

import com.mojang.serialization.Codec;
import com.rezero.mod.ReZeroMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/** Persists profile-bound placements; only explicit commands restore recorded NPCs. */
public final class NpcSpawnManager extends SavedData {
    private static final String DATA_ID = "rezero_npc_spawns";
    private static final Codec<Map<Identifier, NpcSpawnRecord>> MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, NpcSpawnRecord.CODEC);
    private static final Codec<NpcSpawnManager> CODEC = MAP_CODEC.xmap(
            records -> {
                NpcSpawnManager data = new NpcSpawnManager();
                data.records.putAll(records);
                return data;
            }, manager -> Map.copyOf(manager.records)
    );
    private static final SavedDataType<NpcSpawnManager> TYPE = new SavedDataType<>(DATA_ID, NpcSpawnManager::new, CODEC, null);
    private final Map<Identifier, NpcSpawnRecord> records = new HashMap<>();

    public static NpcSpawnManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean recordAndSpawn(ServerLevel level, Identifier profileId, NpcSpawnRecord record) {
        if (!spawn(level, profileId, record)) return false;
        records.put(profileId, record);
        setDirty();
        return true;
    }

    public boolean restore(ServerLevel level, Identifier profileId) {
        NpcSpawnRecord record = records.get(profileId);
        if (record == null || !level.dimension().identifier().toString().equals(record.dimension())) return false;
        return spawn(level, profileId, record);
    }

    public boolean hasRecord(Identifier profileId) {
        return records.containsKey(profileId);
    }

    public Map<Identifier, NpcSpawnRecord> snapshot() {
        return Map.copyOf(records);
    }

    public void restoreSnapshot(ServerLevel level, Map<Identifier, NpcSpawnRecord> snapshot) {
        removeProfileBoundEntities(level);
        records.clear();
        records.putAll(snapshot);
        setDirty();
        restoreAll(level);
    }

    public void restoreAll(ServerLevel level) {
        records.forEach((profileId, record) -> {
            if (!record.dimension().equals(level.dimension().identifier().toString()) || isPresent(level, profileId)) return;
            spawn(level, profileId, record);
        });
    }

    private static boolean isPresent(ServerLevel level, Identifier profileId) {
        String tag = "rezero_npc:" + profileId;
        for (Entity entity : level.getAllEntities()) {
            if (entity.getTags().contains(tag)) return true;
        }
        return false;
    }

    private static void removeProfileBoundEntities(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity.getTags().stream().anyMatch(tag -> tag.startsWith("rezero_npc:"))) entity.discard();
        }
    }

    private static boolean spawn(ServerLevel level, Identifier profileId, NpcSpawnRecord record) {
        NpcProfile profile = NpcProfileLoader.get(profileId);
        if (profile == null) return false;
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(profile.entityType()).orElse(null);
        if (type == null) return false;
        Entity entity = type.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) return false;
        entity.setPos(record.x(), record.y(), record.z());
        entity.setYRot(record.yRot());
        entity.setXRot(record.xRot());
        StoryNpcManager.markProfile(entity, profileId, profile.name());
        level.addFreshEntity(entity);
        ReZeroMod.LOGGER.info("[ReZero-NPC] Spawned {} at {}, {}, {}.", profileId, record.x(), record.y(), record.z());
        return true;
    }
}
