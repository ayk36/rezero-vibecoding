package com.rezero.mod.magic;

import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.registry.ModAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** Server-only spell knowledge mutation API. */
public final class MagicDataManager {
    public static final MagicDataManager INSTANCE = new MagicDataManager();

    private MagicDataManager() {
    }

    public MagicData get(ServerPlayer player) {
        return player.getAttachedOrCreate(ModAttachments.MAGIC_DATA, () -> MagicData.DEFAULT);
    }

    public boolean knows(ServerPlayer player, Identifier spellId) {
        return get(player).learnedSpells().contains(spellId);
    }

    public boolean learn(ServerPlayer player, Identifier spellId) {
        MagicData data = get(player);
        if (data.learnedSpells().contains(spellId)) {
            return false;
        }
        set(player, data.learn(spellId));
        return true;
    }

    public void set(ServerPlayer player, MagicData data) {
        player.setAttached(ModAttachments.MAGIC_DATA, data);
        ReZeroNetworking.sendMagicSummary(player, data);
    }

    public void sync(ServerPlayer player) {
        ReZeroNetworking.sendMagicSummary(player, get(player));
    }
}
