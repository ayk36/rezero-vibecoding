package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server-provided cooldown state; client only displays it. */
public record AbilityCooldownPayload(Identifier abilityId, long readyAtGameTime) implements CustomPacketPayload {
    public static final Type<AbilityCooldownPayload> TYPE = new Type<>(ReZeroMod.id("ability_cooldown"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityCooldownPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, AbilityCooldownPayload::abilityId,
            ByteBufCodecs.VAR_LONG, AbilityCooldownPayload::readyAtGameTime,
            AbilityCooldownPayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
