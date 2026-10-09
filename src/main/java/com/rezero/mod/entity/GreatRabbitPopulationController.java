package com.rezero.mod.entity;

import com.rezero.mod.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;

/** Capped swarm controller: a population boss without unbounded entity spawning. */
public final class GreatRabbitPopulationController {
    private static final String ACTIVE_TAG = "rezero_great_rabbit_swarm";
    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final int MAX_MEMBERS_PER_SWARM = 12;

    private GreatRabbitPopulationController() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) return;
            for (ServerLevel level : server.getAllLevels()) tick(level);
        });
    }

    public static boolean start(ServerLevel level, Player anchor) {
        if (level.getEntitiesOfClass(GreatRabbitEntity.class, anchor.getBoundingBox().inflate(64.0D), rabbit -> rabbit.getTags().contains(ACTIVE_TAG)).size() > 0) {
            return false;
        }
        for (int index = 0; index < 4; index++) spawn(level, anchor, index);
        return true;
    }

    private static void tick(ServerLevel level) {
        for (Player player : level.players()) {
            var members = level.getEntitiesOfClass(GreatRabbitEntity.class, player.getBoundingBox().inflate(48.0D), rabbit -> rabbit.getTags().contains(ACTIVE_TAG));
            if (members.isEmpty()) continue;
            if (members.size() < MAX_MEMBERS_PER_SWARM && level.random.nextInt(3) == 0) spawn(level, player, members.size());
        }
    }

    private static void spawn(ServerLevel level, Player player, int index) {
        var rabbit = ModEntities.GREAT_RABBIT.create(level, EntitySpawnReason.EVENT);
        if (rabbit == null) return;
        rabbit.setPos(player.getX() + (index % 3 - 1) * 2.0D, player.getY(), player.getZ() + (index / 3 - 1) * 2.0D);
        rabbit.addTag(ACTIVE_TAG);
        level.addFreshEntity(rabbit);
    }
}
