package com.rezero.mod.story;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.rezero.mod.world.StoryLocations;
import net.minecraft.core.registries.BuiltInRegistries;

/** Connects authoritative vanilla events to data-driven quest objective tracking. */
public final class QuestEvents {
    private QuestEvents() {
    }

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, killed, source) -> {
            if (killer instanceof ServerPlayer player) {
                StoryManager.recordKill(player, EntityType.getKey(killed.getType()));
                if (killed instanceof net.minecraft.world.entity.LivingEntity living) {
                    com.rezero.mod.player.PlayerDataManager.INSTANCE.grantExperience(player, Math.max(5, Math.round(living.getMaxHealth())));
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                for (Identifier location : java.util.List.of(
                        Identifier.fromNamespaceAndPath("rezero", "roswaal_mansion"),
                        Identifier.fromNamespaceAndPath("rezero", "sanctuary"),
                        Identifier.fromNamespaceAndPath("rezero", "royal_capital")
                )) {
                    if (StoryLocations.isAtLocation((net.minecraft.server.level.ServerLevel) player.level(), location, player.blockPosition(), 24)) {
                        StoryManager.recordLocationReach(player, location);
                    }
                }
                if (!player.isDeadOrDying() && player.level().getDayTime() % 24_000L == 0L) {
                    StoryManager.recordSurvival(player);
                }
                var seen = new java.util.HashSet<net.minecraft.resources.Identifier>();
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    var stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty() && seen.add(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                        StoryManager.recordCollect(player, BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount());
                    }
                }
            }
        });
    }
}
