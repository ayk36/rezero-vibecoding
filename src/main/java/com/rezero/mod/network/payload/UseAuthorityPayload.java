package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client intent only; targets and all effects are validated server-side. */
public record UseAuthorityPayload(Identifier authorityId, int targetEntityId) implements CustomPacketPayload {
    public static final Type<UseAuthorityPayload> TYPE = new Type<>(ReZeroMod.id("use_authority"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseAuthorityPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, UseAuthorityPayload::authorityId,
            ByteBufCodecs.VAR_INT, UseAuthorityPayload::targetEntityId,
            UseAuthorityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
