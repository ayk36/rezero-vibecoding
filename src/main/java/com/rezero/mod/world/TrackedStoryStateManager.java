package com.rezero.mod.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World persistent data for explicitly registered story changes. */
public final class TrackedStoryStateManager extends SavedData {
    private static final String DATA_ID = "rezero_tracked_story_state";
    private static final com.mojang.serialization.Codec<TrackedStoryStateManager> CODEC = TrackedStoryState.CODEC.xmap(
            state -> {
                TrackedStoryStateManager data = new TrackedStoryStateManager();
                data.state = state;
                return data;
            },
            data -> data.state
    );
    private static final SavedDataType<TrackedStoryStateManager> TYPE = new SavedDataType<>(DATA_ID, TrackedStoryStateManager::new, CODEC, null);
    private TrackedStoryState state = TrackedStoryState.EMPTY;

    public static TrackedStoryStateManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public TrackedStoryState snapshot() {
        return state;
    }

    public void restore(TrackedStoryState snapshot) {
        state = snapshot;
        setDirty();
    }

    public void set(String key, String value) {
        state = state.with(key, value);
        setDirty();
    }

    public void remove(String key) {
        if (!state.values().containsKey(key)) return;
        var updated = new java.util.HashMap<>(state.values());
        updated.remove(key);
        state = new TrackedStoryState(updated);
        setDirty();
    }

    public boolean contains(String key) {
        return state.values().containsKey(key);
    }
}
