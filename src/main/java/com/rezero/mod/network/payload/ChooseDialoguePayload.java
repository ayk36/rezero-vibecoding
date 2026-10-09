package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client selection intent. The server verifies it against a current dialogue session. */
public record ChooseDialoguePayload(Identifier dialogueId, String nodeId, int choiceIndex) implements CustomPacketPayload {
    public static final Type<ChooseDialoguePayload> TYPE = new Type<>(ReZeroMod.id("choose_dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseDialoguePayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ChooseDialoguePayload::dialogueId,
            ByteBufCodecs.STRING_UTF8, ChooseDialoguePayload::nodeId,
            ByteBufCodecs.VAR_INT, ChooseDialoguePayload::choiceIndex,
            ChooseDialoguePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
