package com.rezero.mod.network.payload;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.player.PlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Read-only server snapshot used by the HUD and future character screen. */
public record PlayerDataPayload(
        int level, int experience, int mana, int maxMana, int stamina, int maxStamina,
        int strength, int defense, int magicPower, int magicResistance, int speed, int luck
) implements CustomPacketPayload {
    public static final Type<PlayerDataPayload> TYPE = new Type<>(ReZeroMod.id("player_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDataPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayerDataPayload::level,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::experience,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::mana,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::maxMana,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::stamina,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::maxStamina,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::strength,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::defense,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::magicPower,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::magicResistance,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::speed,
            ByteBufCodecs.VAR_INT, PlayerDataPayload::luck,
            PlayerDataPayload::new
    );

    public static PlayerDataPayload from(PlayerData data) {
        return new PlayerDataPayload(
                data.level(), data.experience(), data.mana(), data.maxMana(), data.stamina(), data.maxStamina(),
                data.strength(), data.defense(), data.magicPower(), data.magicResistance(), data.speed(), data.luck()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
