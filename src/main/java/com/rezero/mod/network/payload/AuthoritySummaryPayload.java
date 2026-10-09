package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Client UI snapshot. Ownership and behavior remain server-controlled. */
public record AuthoritySummaryPayload(List<Identifier> authorities, List<Identifier> protections) implements CustomPacketPayload {
    public static final Type<AuthoritySummaryPayload> TYPE = new Type<>(ReZeroMod.id("authority_summary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AuthoritySummaryPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), AuthoritySummaryPayload::authorities,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), AuthoritySummaryPayload::protections,
            AuthoritySummaryPayload::new
    );

    public AuthoritySummaryPayload {
        authorities = List.copyOf(authorities);
        protections = List.copyOf(protections);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
