package com.rezero.mod.player;

import com.rezero.mod.config.ReZeroConfig;
import com.rezero.mod.returnbydeath.ReturnByDeathManager;
import com.rezero.mod.story.StoryDataManager;
import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.magic.MagicDataManager;
import com.rezero.mod.authority.AuthorityManager;
import com.rezero.mod.equipment.EquipmentManager;
import com.rezero.mod.authority.AuthorityRuntime;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/** Lifecycle handlers deliberately run only on the logical server. */
public final class PlayerDataEvents {
    private static final int REGEN_INTERVAL_TICKS = 20;

    private PlayerDataEvents() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ReturnByDeathManager.ensureInitialCheckpoint(handler.player);
            PlayerDataManager.INSTANCE.sync(handler.player);
            MagicDataManager.INSTANCE.sync(handler.player);
            AuthorityManager.INSTANCE.sync(handler.player);
            ReturnByDeathManager.sync(handler.player);
            ReZeroNetworking.sendQuestSummary(handler.player, StoryDataManager.INSTANCE.get(handler.player));
            ReZeroNetworking.sendRelationshipSummary(handler.player, StoryDataManager.INSTANCE.get(handler.player));
            ReZeroNetworking.sendFactionSummary(handler.player, StoryDataManager.INSTANCE.get(handler.player));
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            newPlayer.setAttached(com.rezero.mod.registry.ModAttachments.PLAYER_DATA, PlayerDataManager.INSTANCE.getPlayerData(oldPlayer));
            newPlayer.setAttached(com.rezero.mod.registry.ModAttachments.MAGIC_DATA, MagicDataManager.INSTANCE.get(oldPlayer));
            newPlayer.setAttached(com.rezero.mod.registry.ModAttachments.AUTHORITY_DATA, AuthorityManager.INSTANCE.get(oldPlayer));
            newPlayer.setAttached(com.rezero.mod.registry.ModAttachments.EQUIPMENT_DATA, EquipmentManager.INSTANCE.get(oldPlayer));
            newPlayer.setAttached(com.rezero.mod.registry.ModAttachments.STORY_DATA, StoryDataManager.INSTANCE.get(oldPlayer));
            ReturnByDeathManager.copy(oldPlayer, newPlayer);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % REGEN_INTERVAL_TICKS != 0) {
                return;
            }

            ReZeroConfig.Values config = ReZeroConfig.get();
            for (var player : server.getPlayerList().getPlayers()) {
                PlayerDataManager.INSTANCE.regenerate(player, config.manaRegeneration(), config.staminaRegeneration());
                AuthorityRuntime.INSTANCE.onTick(player);
            }
        });
    }
}
