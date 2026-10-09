package com.rezero.mod.returnbydeath;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.config.ReZeroConfig;
import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.player.PlayerDataManager;
import com.rezero.mod.registry.ModAttachments;
import com.rezero.mod.story.StoryDataManager;
import com.rezero.mod.world.TrackedStoryStateManager;
import com.rezero.mod.world.TrackedStoryBlockManager;
import com.rezero.mod.magic.MagicDataManager;
import com.rezero.mod.npc.NpcSpawnManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side Return by Death controller. It cancels fatal damage before vanilla's death screen. */
public final class ReturnByDeathManager {
    private static final Set<UUID> PENDING_RESTORES = ConcurrentHashMap.newKeySet();

    private ReturnByDeathManager() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player) || !canRestore(player)) {
                return true;
            }

            UUID playerId = player.getUUID();
            if (!PENDING_RESTORES.add(playerId)) {
                return false;
            }

            // A canceled death must still leave a positive health value for the next server tick.
            player.setHealth(1.0F);
            player.level().getServer().execute(() -> restore(player));
            return false;
        });
    }

    public static void ensureInitialCheckpoint(ServerPlayer player) {
        ReturnByDeathData data = get(player);
        if (ReZeroConfig.get().enableReturnByDeath() && data.checkpoint().isEmpty()) {
            set(player, data.unlocked(true).withCheckpoint(capture(player)));
        }
    }

    public static void createCheckpoint(ServerPlayer player) {
        ReturnByDeathData data = get(player);
        set(player, data.unlocked(true).withCheckpoint(capture(player)));
        player.sendSystemMessage(Component.translatable("message.rezero.checkpoint_set"));
    }

    public static void copy(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        newPlayer.setAttached(ModAttachments.RETURN_BY_DEATH, get(oldPlayer));
    }

    public static void sync(ServerPlayer player) {
        ReturnByDeathData data = get(player);
        ReZeroNetworking.sendReturnByDeath(player, data.unlocked(), data.checkpoint().isPresent());
    }

    public static ReturnByDeathData get(ServerPlayer player) {
        return player.getAttachedOrCreate(ModAttachments.RETURN_BY_DEATH, () -> ReturnByDeathData.DEFAULT);
    }

    private static void set(ServerPlayer player, ReturnByDeathData data) {
        player.setAttached(ModAttachments.RETURN_BY_DEATH, data);
        sync(player);
    }

    private static boolean canRestore(ServerPlayer player) {
        if (!ReZeroConfig.get().enableReturnByDeath()) {
            return false;
        }

        ReturnByDeathData data = get(player);
        return data.unlocked() && data.checkpoint().isPresent();
    }

    private static Checkpoint capture(ServerPlayer player) {
        List<net.minecraft.world.item.ItemStack> inventory = new java.util.ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }

        return new Checkpoint(
                player.level().dimension().identifier().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot(),
                player.getHealth(),
                PlayerDataManager.INSTANCE.getPlayerData(player),
                StoryDataManager.INSTANCE.get(player),
                MagicDataManager.INSTANCE.get(player),
                TrackedStoryStateManager.get((ServerLevel) player.level()).snapshot(),
                NpcSpawnManager.get((ServerLevel) player.level()).snapshot(),
                inventory
        );
    }

    private static void restore(ServerPlayer player) {
        try {
            ReturnByDeathData data = get(player);
            Optional<Checkpoint> optionalCheckpoint = data.checkpoint();
            if (optionalCheckpoint.isEmpty()) {
                return;
            }

            Checkpoint checkpoint = optionalCheckpoint.get();
            Identifier dimensionId = Identifier.parse(checkpoint.dimension());
            ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId);
            ServerLevel destination = player.level().getServer().getLevel(dimensionKey);
            if (destination == null) {
                ReZeroMod.LOGGER.error("[ReZero-RBD] Checkpoint dimension {} is unavailable for {}.", dimensionId, player.getName().getString());
                return;
            }

            player.getInventory().clearContent();
            for (int slot = 0; slot < checkpoint.inventory().size() && slot < player.getInventory().getContainerSize(); slot++) {
                player.getInventory().setItem(slot, checkpoint.inventory().get(slot).copy());
            }

            var currentStory = StoryDataManager.INSTANCE.get(player);
            var currentMagic = MagicDataManager.INSTANCE.get(player);
            PlayerDataManager.INSTANCE.set(player, checkpoint.playerData());
            StoryDataManager.INSTANCE.set(player, checkpoint.storyData().mergeRetainedMemory(currentStory));
            MagicDataManager.INSTANCE.set(player, checkpoint.magicData().mergeKnowledge(currentMagic));
            TrackedStoryStateManager manager = TrackedStoryStateManager.get(destination);
            manager.restore(checkpoint.trackedStoryState());
            TrackedStoryBlockManager.apply(destination, manager.snapshot());
            NpcSpawnManager.get(destination).restoreSnapshot(destination, checkpoint.npcSpawns());
            player.setHealth(Math.clamp(checkpoint.health(), 1.0F, player.getMaxHealth()));
            player.teleportTo(destination, checkpoint.x(), checkpoint.y(), checkpoint.z(), Set.of(), checkpoint.yRot(), checkpoint.xRot(), false);
            set(player, data.restoredAt(destination.getGameTime()));
            ReZeroNetworking.playReturnByDeathEffect(player);
            player.sendSystemMessage(Component.translatable("message.rezero.returning"));
            ReZeroMod.LOGGER.info("[ReZero-RBD] Restored {} to checkpoint in {}.", player.getName().getString(), dimensionId);
        } catch (Exception exception) {
            ReZeroMod.LOGGER.error("[ReZero-RBD] Failed to restore {}.", player.getName().getString(), exception);
        } finally {
            PENDING_RESTORES.remove(player.getUUID());
        }
    }
}
