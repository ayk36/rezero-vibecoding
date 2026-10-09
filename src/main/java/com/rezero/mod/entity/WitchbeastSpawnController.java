package com.rezero.mod.entity;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.config.ReZeroConfig;
import com.rezero.mod.registry.ModEntities;
import com.rezero.mod.story.StoryDataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;

/** Low-frequency, capped Wolgarm spawning. No world scans or client authority. */
public final class WitchbeastSpawnController {
    private static final int CHECK_INTERVAL_TICKS = 20 * 30;
    private static final int MAX_WOLGARMS_PER_PLAYER = 4;

    private WitchbeastSpawnController() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0 || ReZeroConfig.get().mobSpawnRate() <= 0.0D) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                trySpawn(player);
            }
        });
    }

    private static void trySpawn(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD)
                || isDaytime(player.level().getDayTime())
                || !StoryDataManager.INSTANCE.get(player).flags().contains("visited_mansion")) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        long nearby = level.getEntitiesOfClass(WolgarmEntity.class, player.getBoundingBox().inflate(48.0D)).size();
        if (nearby >= MAX_WOLGARMS_PER_PLAYER) {
            return;
        }

        int x = player.getBlockX() + level.random.nextInt(25) - 12;
        int z = player.getBlockZ() + level.random.nextInt(25) - 12;
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos position = new BlockPos(x, y, z);
        if (!level.getBlockState(position.below()).isSolidRender() || !level.getBlockState(position).isAir()) {
            return;
        }
        WolgarmEntity wolgarm = ModEntities.WOLGARM.create(level, EntitySpawnReason.EVENT);
        if (wolgarm == null) {
            return;
        }
        wolgarm.setPos(x + 0.5D, y, z + 0.5D);
        wolgarm.setTarget(player);
        level.addFreshEntity(wolgarm);
        ReZeroMod.LOGGER.debug("[ReZero] Spawned Wolgarm near {}.", player.getName().getString());
    }

    private static boolean isDaytime(long dayTime) {
        long time = Math.floorMod(dayTime, 24_000L);
        return time >= 1_000L && time < 13_000L;
    }
}
