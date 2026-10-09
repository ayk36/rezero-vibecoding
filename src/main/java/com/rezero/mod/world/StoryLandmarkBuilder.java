package com.rezero.mod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Minimal generated landmark shells for development worlds, deliberately bounded and reversible by RBD state. */
public final class StoryLandmarkBuilder {
    private StoryLandmarkBuilder() {
    }

    public static boolean buildMansionMarker(ServerLevel level, BlockPos center) {
        return build(level, center, 8, Blocks.DARK_OAK_PLANKS.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
    }

    public static boolean buildSanctuaryMarker(ServerLevel level, BlockPos center) {
        return build(level, center, 7, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState());
    }

    public static boolean buildCapitalMarker(ServerLevel level, BlockPos center) {
        return build(level, center, 9, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
    }

    private static boolean build(ServerLevel level, BlockPos center, int radius, net.minecraft.world.level.block.state.BlockState wall, net.minecraft.world.level.block.state.BlockState light) {
        if (!level.getBlockState(center).isAir()) return false;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean edge = Math.abs(x) == radius || Math.abs(z) == radius;
                if (edge) {
                    level.setBlockAndUpdate(center.offset(x, 0, z), wall);
                    level.setBlockAndUpdate(center.offset(x, 1, z), wall);
                    level.setBlockAndUpdate(center.offset(x, 2, z), wall);
                }
            }
        }
        level.setBlockAndUpdate(center, light);
        return true;
    }
}
