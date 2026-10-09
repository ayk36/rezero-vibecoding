package com.rezero.mod.magic;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Reload-safe datapack registry. A bad spell is isolated rather than crashing a world. */
public final class SpellRegistry {
    private static Map<Identifier, SpellDefinition> spells = Map.of();

    private SpellRegistry() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(SpellRegistry::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) {
                reload(server);
            }
        });
    }

    public static SpellDefinition get(Identifier id) {
        return spells.get(id);
    }

    public static Map<Identifier, SpellDefinition> all() {
        return spells;
    }

    private static void reload(net.minecraft.server.MinecraftServer server) {
        Map<Identifier, SpellDefinition> loaded = new HashMap<>();
        server.getResourceManager().listResources("spells", id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier spellId = Identifier.fromNamespaceAndPath(
                    fileId.getNamespace(), fileId.getPath().substring("spells/".length(), fileId.getPath().length() - ".json".length())
            );
            try (var reader = resource.openAsReader()) {
                SpellDefinition.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero-Magic] Invalid spell {}: {}", fileId, error))
                        .ifPresent(definition -> loaded.put(spellId, definition));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero-Magic] Failed to load spell {}.", fileId, exception);
            }
        });
        spells = Map.copyOf(loaded);
        ReZeroMod.LOGGER.info("[ReZero-Magic] Loaded {} spell definitions.", spells.size());
    }
}
