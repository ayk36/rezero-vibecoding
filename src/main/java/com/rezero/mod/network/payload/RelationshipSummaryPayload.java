package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Read-only relationship snapshot for client UI. */
public record RelationshipSummaryPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Entry(Identifier npc, int value) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Entry::npc,
                ByteBufCodecs.VAR_INT, Entry::value,
                Entry::new
        );
    }
    public static final Type<RelationshipSummaryPayload> TYPE = new Type<>(ReZeroMod.id("relationship_summary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RelationshipSummaryPayload> CODEC = StreamCodec.composite(
            Entry.CODEC.apply(ByteBufCodecs.list()), RelationshipSummaryPayload::entries,
            RelationshipSummaryPayload::new
    );
    public RelationshipSummaryPayload { entries = List.copyOf(entries); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
