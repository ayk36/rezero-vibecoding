package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Render-only dialogue node supplied after server validation. */
public record OpenDialoguePayload(Identifier dialogueId, String nodeId, String speaker, String text, List<Choice> choices) implements CustomPacketPayload {
    public record Choice(int index, String text) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Choice> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Choice::index,
                ByteBufCodecs.STRING_UTF8, Choice::text,
                Choice::new
        );
    }
    public static final Type<OpenDialoguePayload> TYPE = new Type<>(ReZeroMod.id("open_dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialoguePayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, OpenDialoguePayload::dialogueId,
            ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::nodeId,
            ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::speaker,
            ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::text,
            Choice.CODEC.apply(ByteBufCodecs.list()), OpenDialoguePayload::choices,
            OpenDialoguePayload::new
    );

    public OpenDialoguePayload {
        choices = List.copyOf(choices);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
