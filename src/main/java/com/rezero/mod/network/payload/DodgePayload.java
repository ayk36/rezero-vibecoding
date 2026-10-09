package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client movement intent. Stamina, cooldown, and velocity are server-defined. */
public record DodgePayload() implements CustomPacketPayload {
    public static final Type<DodgePayload> TYPE = new Type<>(ReZeroMod.id("dodge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DodgePayload> CODEC = StreamCodec.unit(new DodgePayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
