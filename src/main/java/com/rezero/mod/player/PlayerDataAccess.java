package com.rezero.mod.player;

import net.minecraft.server.level.ServerPlayer;

public interface PlayerDataAccess {
    PlayerData getPlayerData(ServerPlayer player);

    void sync(ServerPlayer player);

    void markDirty(ServerPlayer player);

    void reset(ServerPlayer player);
}
