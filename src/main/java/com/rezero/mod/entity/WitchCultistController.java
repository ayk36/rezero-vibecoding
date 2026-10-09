package com.rezero.mod.entity;

import com.rezero.mod.registry.ModEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** Adds Witch Cult behavior to explicitly marked vanilla hostile adapters. */
public final class WitchCultistController {
    private static final String TAG = "rezero_witch_cultist";

    private WitchCultistController() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 40 != 0) return;
            for (ServerLevel level : server.getAllLevels()) {
                var visited = new java.util.HashSet<java.util.UUID>();
                for (Player player : level.players()) {
                    for (Mob cultist : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32.0D), mob -> mob.getTags().contains(TAG))) {
                        if (!visited.add(cultist.getUUID())) continue;
                        Player target = level.getNearestPlayer(cultist, 18.0D);
                        if (target != null) {
                            cultist.setTarget(target);
                            if (cultist.distanceToSqr(target) <= 9.0D) target.addEffect(new MobEffectInstance(ModEffects.curseHolder(), 80, 0));
                        }
                    }
                }
            }
        });
    }

    public static void mark(Mob entity) {
        entity.addTag(TAG);
        entity.setCustomName(net.minecraft.network.chat.Component.literal("Witch Cultist"));
        entity.setCustomNameVisible(true);
    }
}
