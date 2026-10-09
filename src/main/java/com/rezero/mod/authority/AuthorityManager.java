package com.rezero.mod.authority;

import com.rezero.mod.registry.ModAttachments;
import com.rezero.mod.network.ReZeroNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** Server mutation API for authority/protection inventory. */
public final class AuthorityManager {
    public static final AuthorityManager INSTANCE = new AuthorityManager();

    private AuthorityManager() {
    }

    public AuthorityData get(ServerPlayer player) {
        return player.getAttachedOrCreate(ModAttachments.AUTHORITY_DATA, () -> AuthorityData.DEFAULT);
    }

    public boolean grantAuthority(ServerPlayer player, Identifier id) {
        AuthorityData data = get(player);
        if (data.authorities().contains(id)) {
            return false;
        }
        set(player, data.grantAuthority(id));
        return true;
    }

    public boolean grantProtection(ServerPlayer player, Identifier id) {
        AuthorityData data = get(player);
        if (data.divineProtections().contains(id)) {
            return false;
        }
        set(player, data.grantProtection(id));
        return true;
    }

    public void set(ServerPlayer player, AuthorityData data) {
        player.setAttached(ModAttachments.AUTHORITY_DATA, data);
        ReZeroNetworking.sendAuthoritySummary(player, data);
    }

    public void sync(ServerPlayer player) {
        ReZeroNetworking.sendAuthoritySummary(player, get(player));
    }
}
