package com.rezero.mod.world;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.rezero.mod.boss.BossRegistry;

/** Reload-safe world event definitions. */
public final class WorldEventRegistry {
    private static Map<Identifier, WorldEventDefinition> events = Map.of();

    private WorldEventRegistry() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(WorldEventRegistry::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) reload(server);
        });
    }

    public static WorldEventDefinition get(Identifier id) {
        return events.get(id);
    }

    public static Map<Identifier, WorldEventDefinition> all() {
        return events;
    }

    private static void reload(net.minecraft.server.MinecraftServer server) {
        Map<Identifier, WorldEventDefinition> loaded = new HashMap<>();
        server.getResourceManager().listResources("world_events", id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier eventId = Identifier.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath().substring("world_events/".length(), fileId.getPath().length() - ".json".length()));
            try (var reader = resource.openAsReader()) {
                WorldEventDefinition.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero-World] Invalid event {}: {}", fileId, error))
                        .ifPresent(definition -> loaded.put(eventId, definition));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero-World] Failed to load event {}.", fileId, exception);
            }
        });
        events = Map.copyOf(loaded);
        events.forEach((eventId, definition) -> {
            if (BossRegistry.get(definition.boss()) == null) {
                ReZeroMod.LOGGER.error("[ReZero-World] Event {} references unavailable boss {}.", eventId, definition.boss());
            }
        });
        ReZeroMod.LOGGER.info("[ReZero-World] Loaded {} world event definitions.", events.size());
    }
}
