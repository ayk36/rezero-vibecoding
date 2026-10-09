package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Read-only spell knowledge snapshot for the magic client screen. */
public record MagicSummaryPayload(List<Identifier> learnedSpells) implements CustomPacketPayload {
    public static final Type<MagicSummaryPayload> TYPE = new Type<>(ReZeroMod.id("magic_summary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MagicSummaryPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), MagicSummaryPayload::learnedSpells,
            MagicSummaryPayload::new
    );

    public MagicSummaryPayload {
        learnedSpells = List.copyOf(learnedSpells);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
