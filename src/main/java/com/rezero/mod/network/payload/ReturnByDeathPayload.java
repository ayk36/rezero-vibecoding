package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ReturnByDeathPayload(boolean unlocked, boolean checkpointAvailable, boolean restoring) implements CustomPacketPayload {
    public static final Type<ReturnByDeathPayload> TYPE = new Type<>(ReZeroMod.id("return_by_death"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReturnByDeathPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ReturnByDeathPayload::unlocked,
            ByteBufCodecs.BOOL, ReturnByDeathPayload::checkpointAvailable,
            ByteBufCodecs.BOOL, ReturnByDeathPayload::restoring,
            ReturnByDeathPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
