package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client intent only. The server decides whether a checkpoint can be created. */
public record CreateCheckpointPayload() implements CustomPacketPayload {
    public static final Type<CreateCheckpointPayload> TYPE = new Type<>(ReZeroMod.id("create_checkpoint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateCheckpointPayload> CODEC = StreamCodec.unit(new CreateCheckpointPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
