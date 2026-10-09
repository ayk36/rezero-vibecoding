package com.rezero.mod.world;

import com.rezero.mod.ReZeroMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.resources.Identifier;

/** Lightweight landmark placement for the MVP story route. Avoids expensive world-wide scans. */
public final class StoryLocations {
    private StoryLocations() {
    }

    public static boolean locateMansion(ServerLevel level, BlockPos center) {
        return register(level, center, "roswaal_mansion", "Roswaal Mansion");
    }

    public static boolean locateSanctuary(ServerLevel level, BlockPos center) {
        return register(level, center, "sanctuary", "Sanctuary");
    }

    public static boolean locateRoyalCapital(ServerLevel level, BlockPos center) {
        return register(level, center, "royal_capital", "Royal Capital");
    }

    private static boolean register(ServerLevel level, BlockPos center, String key, String name) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ());
        BlockPos origin = new BlockPos(center.getX(), y, center.getZ());
        var manager = TrackedStoryStateManager.get(level);
        if ("placed".equals(manager.snapshot().values().get(key))) return false;
        manager.set(key, "placed");
        manager.set(key + ".x", Integer.toString(origin.getX()));
        manager.set(key + ".y", Integer.toString(origin.getY()));
        manager.set(key + ".z", Integer.toString(origin.getZ()));
        ReZeroMod.LOGGER.info("[ReZero-World] Registered {} landmark at {}.", name, origin);
        return true;
    }

    public static boolean isAtLocation(ServerLevel level, Identifier locationId, BlockPos position, int radius) {
        String key = switch (locationId.toString()) {
            case "rezero:roswaal_mansion" -> "roswaal_mansion";
            case "rezero:sanctuary" -> "sanctuary";
            case "rezero:royal_capital" -> "royal_capital";
            default -> null;
        };
        if (key == null) return false;
        var values = TrackedStoryStateManager.get(level).snapshot().values();
        try {
            int x = Integer.parseInt(values.get(key + ".x"));
            int y = Integer.parseInt(values.get(key + ".y"));
            int z = Integer.parseInt(values.get(key + ".z"));
            return position.distSqr(new BlockPos(x, y, z)) <= (long) radius * radius;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
