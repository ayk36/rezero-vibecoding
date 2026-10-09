package com.rezero.mod.player;

import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.registry.ModAttachments;
import com.rezero.mod.save.MigrationHandler;
import net.minecraft.server.level.ServerPlayer;

/** Server-only mutation API. Clients only receive synchronized snapshots. */
public final class PlayerDataManager implements PlayerDataAccess {
    public static final PlayerDataManager INSTANCE = new PlayerDataManager();

    private PlayerDataManager() {
    }

    @Override
    public PlayerData getPlayerData(ServerPlayer player) {
        return MigrationHandler.migratePlayerData(player.getAttachedOrCreate(ModAttachments.PLAYER_DATA, () -> PlayerData.DEFAULT));
    }

    public void set(ServerPlayer player, PlayerData data) {
        player.setAttached(ModAttachments.PLAYER_DATA, data);
        sync(player);
    }

    public boolean consumeResources(ServerPlayer player, int manaCost, int staminaCost) {
        if (manaCost < 0 || staminaCost < 0) {
            return false;
        }

        PlayerData data = getPlayerData(player);
        if (data.mana() < manaCost || data.stamina() < staminaCost) {
            return false;
        }

        set(player, data.withMana(data.mana() - manaCost).withStamina(data.stamina() - staminaCost));
        return true;
    }

    public void regenerate(ServerPlayer player, int manaAmount, int staminaAmount) {
        PlayerData data = getPlayerData(player);
        PlayerData regenerated = data.regenerate(manaAmount, staminaAmount);
        if (!regenerated.equals(data)) {
            set(player, regenerated);
        }
    }

    public void grantExperience(ServerPlayer player, int amount) {
        PlayerData data = getPlayerData(player);
        PlayerData progressed = data.addExperience(amount);
        if (!progressed.equals(data)) set(player, progressed);
    }

    @Override
    public void sync(ServerPlayer player) {
        ReZeroNetworking.sendPlayerData(player, getPlayerData(player));
    }

    @Override
    public void markDirty(ServerPlayer player) {
        player.setAttached(ModAttachments.PLAYER_DATA, getPlayerData(player));
    }

    @Override
    public void reset(ServerPlayer player) {
        set(player, PlayerData.DEFAULT);
    }
}
