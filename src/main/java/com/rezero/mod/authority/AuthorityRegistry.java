package com.rezero.mod.authority;

import com.google.gson.JsonParser;
import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Reload-safe authority/protection definitions. Invalid content is isolated. */
public final class AuthorityRegistry {
    private static Map<Identifier, AuthorityDefinition> authorities = Map.of();
    private static Map<Identifier, DivineProtectionDefinition> protections = Map.of();

    private AuthorityRegistry() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(AuthorityRegistry::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) reload(server);
        });
    }

    public static AuthorityDefinition authority(Identifier id) { return authorities.get(id); }
    public static DivineProtectionDefinition protection(Identifier id) { return protections.get(id); }

    private static void reload(net.minecraft.server.MinecraftServer server) {
        authorities = load(server, "authorities", AuthorityDefinition.CODEC);
        protections = load(server, "divine_protections", DivineProtectionDefinition.CODEC);
        ReZeroMod.LOGGER.info("[ReZero] Loaded {} authorities and {} divine protections.", authorities.size(), protections.size());
    }

    private static <T> Map<Identifier, T> load(net.minecraft.server.MinecraftServer server, String directory, com.mojang.serialization.Codec<T> codec) {
        Map<Identifier, T> loaded = new HashMap<>();
        server.getResourceManager().listResources(directory, id -> id.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath().substring(directory.length() + 1, fileId.getPath().length() - ".json".length()));
            try (var reader = resource.openAsReader()) {
                codec.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ReZeroMod.LOGGER.error("[ReZero] Invalid {} {}: {}", directory, fileId, error))
                        .ifPresent(value -> loaded.put(id, value));
            } catch (IOException exception) {
                ReZeroMod.LOGGER.error("[ReZero] Failed to load {} {}.", directory, fileId, exception);
            }
        });
        return Map.copyOf(loaded);
    }
}
