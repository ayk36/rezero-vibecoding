package com.rezero.mod.npc;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Loads profiles from the entities data directory; invalid entries are isolated. */
public final class NpcProfileLoader {
    private static Map<Identifier, NpcProfile> profiles = Map.of();

    private NpcProfileLoader() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> reload(server));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                reload(server);
            }
        });
    }

    public static NpcProfile get(Identifier id) {
        return profiles.get(id);
    }

    private static void reload(net.minecraft.server.MinecraftServer server) {
        Map<Identifier, NpcProfile> loaded = new HashMap<>();
        server.getResourceManager().listResources("entities", id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier profileId = Identifier.fromNamespaceAndPath(
                    fileId.getNamespace(), fileId.getPath().substring("entities/".length(), fileId.getPath().length() - ".json".length())
            );
            try (var reader = resource.openAsReader()) {
                NpcProfile.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero-NPC] Invalid profile {}: {}", fileId, error))
                        .ifPresent(profile -> loaded.put(profileId, profile));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero-NPC] Failed to load profile {}.", fileId, exception);
            }
        });
        profiles = Map.copyOf(loaded);
        ReZeroMod.LOGGER.info("[ReZero-NPC] Loaded {} NPC profiles.", profiles.size());
    }
}
