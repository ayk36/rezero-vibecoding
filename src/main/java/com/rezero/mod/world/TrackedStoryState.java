package com.rezero.mod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

/** Small, explicit rollback state for story objects; never a full-world snapshot. */
public record TrackedStoryState(Map<String, String> values) {
    public static final TrackedStoryState EMPTY = new TrackedStoryState(Map.of());
    public static final Codec<TrackedStoryState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("values", Map.of()).forGetter(TrackedStoryState::values)
    ).apply(instance, TrackedStoryState::new));

    public TrackedStoryState {
        values = Map.copyOf(values);
    }

    public TrackedStoryState with(String key, String value) {
        var updated = new java.util.HashMap<>(values);
        updated.put(key, value);
        return new TrackedStoryState(updated);
    }
}
