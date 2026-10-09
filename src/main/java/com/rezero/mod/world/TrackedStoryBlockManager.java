package com.rezero.mod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

/** Explicit story-block registry. Only registered block state is eligible for RBD restoration. */
public final class TrackedStoryBlockManager {
    private static final String PREFIX = "block.";

    private TrackedStoryBlockManager() { }

    public static void track(ServerLevel level, BlockPos pos) {
        Identifier block = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
        TrackedStoryStateManager.get(level).set(key(pos), block.toString());
    }

    public static void apply(ServerLevel level, TrackedStoryState state) {
        state.values().forEach((key, rawBlock) -> {
            if (!key.startsWith(PREFIX)) return;
            BlockPos pos = parsePosition(key);
            Identifier blockId = Identifier.tryParse(rawBlock);
            if (pos == null || blockId == null) return;
            BuiltInRegistries.BLOCK.getOptional(blockId).ifPresent(block -> level.setBlockAndUpdate(pos, block.defaultBlockState()));
        });
    }

    private static String key(BlockPos pos) {
        return PREFIX + pos.getX() + "." + pos.getY() + "." + pos.getZ();
    }

    private static BlockPos parsePosition(String key) {
        String[] values = key.substring(PREFIX.length()).split("\\.");
        if (values.length != 3) return null;
        try {
            return new BlockPos(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
