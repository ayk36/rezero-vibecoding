package com.rezero.mod.world;

import com.rezero.mod.story.StoryDataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Periodically exposes eligible events without scanning entities or trusting clients. */
public final class WorldEventController {
    private static final int CHECK_INTERVAL_TICKS = 20 * 30;

    private WorldEventController() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) {
                return;
            }
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null || !overworld.isThundering() || isDaytime(overworld.getDayTime())) {
                return;
            }
            for (var entry : WorldEventRegistry.all().entrySet()) {
                WorldEventManager events = WorldEventManager.get(overworld);
                if (events.status(entry.getKey()) != WorldEventStatus.INACTIVE) {
                    continue;
                }
                if (hasEligiblePlayer(server.getPlayerList().getPlayers(), entry.getValue().requiredStoryFlag())) {
                    events.transition(entry.getKey(), WorldEventStatus.INACTIVE, WorldEventStatus.AVAILABLE);
                }
            }
        });
    }

    private static boolean hasEligiblePlayer(java.util.List<ServerPlayer> players, String requiredFlag) {
        return players.stream().anyMatch(player -> requiredFlag.isBlank() || StoryDataManager.INSTANCE.get(player).flags().contains(requiredFlag));
    }

    private static boolean isDaytime(long dayTime) {
        long time = Math.floorMod(dayTime, 24_000L);
        return time >= 1_000L && time < 13_000L;
    }
}
