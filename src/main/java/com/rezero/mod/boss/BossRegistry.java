package com.rezero.mod.boss;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Safe datapack boss definition loader. */
public final class BossRegistry {
    private static Map<Identifier, BossDefinition> bosses = Map.of();

    private BossRegistry() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(BossRegistry::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) {
                reload(server);
            }
        });
    }

    public static BossDefinition get(Identifier id) {
        return bosses.get(id);
    }

    private static void reload(net.minecraft.server.MinecraftServer server) {
        Map<Identifier, BossDefinition> loaded = new HashMap<>();
        server.getResourceManager().listResources("bosses", id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier bossId = Identifier.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath().substring("bosses/".length(), fileId.getPath().length() - ".json".length()));
            try (var reader = resource.openAsReader()) {
                BossDefinition.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero-Boss] Invalid boss {}: {}", fileId, error))
                        .ifPresent(definition -> loaded.put(bossId, definition));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero-Boss] Failed to load boss {}.", fileId, exception);
            }
        });
        bosses = Map.copyOf(loaded);
        validatePhases();
        ReZeroMod.LOGGER.info("[ReZero-Boss] Loaded {} boss definitions.", bosses.size());
    }

    private static void validatePhases() {
        bosses.forEach((id, definition) -> {
            float previous = Float.POSITIVE_INFINITY;
            for (BossPhase phase : definition.phases()) {
                if (phase.healthThreshold() > previous) {
                    ReZeroMod.LOGGER.error("[ReZero-Boss] Boss {} phases must be descending by health threshold.", id);
                    break;
                }
                previous = phase.healthThreshold();
            }
        });
    }
}
