package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client intent. It contains no damage, resource, reward, or teleport authority. */
public record UseAbilityPayload(Identifier abilityId, int targetEntityId, float directionX, float directionY, float directionZ) implements CustomPacketPayload {
    public static final Type<UseAbilityPayload> TYPE = new Type<>(ReZeroMod.id("use_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, UseAbilityPayload::abilityId,
            ByteBufCodecs.VAR_INT, UseAbilityPayload::targetEntityId,
            ByteBufCodecs.FLOAT, UseAbilityPayload::directionX,
            ByteBufCodecs.FLOAT, UseAbilityPayload::directionY,
            ByteBufCodecs.FLOAT, UseAbilityPayload::directionZ,
            UseAbilityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
