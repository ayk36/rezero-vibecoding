package com.rezero.mod.economy;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Reload-safe trade registry. */
public final class TradeRegistry {
    private static Map<Identifier, TradeDefinition> trades = Map.of();
    private TradeRegistry() { }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(TradeRegistry::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> { if (success) reload(server); });
    }
    public static TradeDefinition get(Identifier id) { return trades.get(id); }

    private static void reload(net.minecraft.server.MinecraftServer server) {
        Map<Identifier, TradeDefinition> loaded = new HashMap<>();
        server.getResourceManager().listResources("trades", id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath().substring("trades/".length(), fileId.getPath().length() - ".json".length()));
            try (var reader = resource.openAsReader()) {
                TradeDefinition.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero] Invalid trade {}: {}", fileId, error))
                        .ifPresent(value -> loaded.put(id, value));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero] Failed to load trade {}.", fileId, exception);
            }
        });
        trades = Map.copyOf(loaded);
        ReZeroMod.LOGGER.info("[ReZero] Loaded {} trades.", trades.size());
    }
}
