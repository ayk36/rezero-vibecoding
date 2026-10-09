package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Read-only faction reputation snapshot for client UI. */
public record FactionSummaryPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Entry(Identifier faction, int value) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Entry::faction,
                ByteBufCodecs.VAR_INT, Entry::value,
                Entry::new
        );
    }
    public static final Type<FactionSummaryPayload> TYPE = new Type<>(ReZeroMod.id("faction_summary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FactionSummaryPayload> CODEC = StreamCodec.composite(
            Entry.CODEC.apply(ByteBufCodecs.list()), FactionSummaryPayload::entries,
            FactionSummaryPayload::new
    );
    public FactionSummaryPayload { entries = List.copyOf(entries); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
