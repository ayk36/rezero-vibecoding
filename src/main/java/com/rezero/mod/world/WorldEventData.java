package com.rezero.mod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;

/** Server world-persistent state for scheduled/story events. */
public record WorldEventData(Map<Identifier, WorldEventStatus> statuses) {
    public static final WorldEventData DEFAULT = new WorldEventData(Map.of());
    private static final Codec<WorldEventStatus> STATUS_CODEC = Codec.STRING.xmap(WorldEventStatus::valueOf, WorldEventStatus::name);
    public static final Codec<WorldEventData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, STATUS_CODEC).optionalFieldOf("statuses", Map.of()).forGetter(WorldEventData::statuses)
    ).apply(instance, WorldEventData::new));

    public WorldEventData {
        statuses = Map.copyOf(statuses);
    }

    public WorldEventData with(Identifier id, WorldEventStatus status) {
        var updated = new java.util.HashMap<>(statuses);
        updated.put(id, status);
        return new WorldEventData(updated);
    }
}
