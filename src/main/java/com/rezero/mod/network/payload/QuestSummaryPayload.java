package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Compact server snapshot used only to render the client quest screen. */
public record QuestSummaryPayload(List<Entry> quests) implements CustomPacketPayload {
    public record Entry(Identifier id, String title, String status, int completedObjectives, int objectiveCount) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Entry::id,
                ByteBufCodecs.STRING_UTF8, Entry::title,
                ByteBufCodecs.STRING_UTF8, Entry::status,
                ByteBufCodecs.VAR_INT, Entry::completedObjectives,
                ByteBufCodecs.VAR_INT, Entry::objectiveCount,
                Entry::new
        );
    }

    public static final Type<QuestSummaryPayload> TYPE = new Type<>(ReZeroMod.id("quest_summary"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestSummaryPayload> CODEC = Entry.CODEC.apply(ByteBufCodecs.list()).map(QuestSummaryPayload::new, QuestSummaryPayload::quests);

    public QuestSummaryPayload {
        quests = List.copyOf(quests);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
