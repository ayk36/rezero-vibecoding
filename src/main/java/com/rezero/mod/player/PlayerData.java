package com.rezero.mod.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Immutable, persistent RPG state. Mutations must go through PlayerDataManager. */
public record PlayerData(
        int level,
        int experience,
        int mana,
        int maxMana,
        int stamina,
        int maxStamina,
        int strength,
        int defense,
        int magicPower,
        int magicResistance,
        int speed,
        int luck
) {
    public static final int MAX_LEVEL = 100;
    public static final PlayerData DEFAULT = new PlayerData(1, 0, 100, 100, 100, 100, 0, 0, 0, 0, 0, 0);

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(PlayerData::level),
            Codec.INT.fieldOf("experience").forGetter(PlayerData::experience),
            Codec.INT.fieldOf("mana").forGetter(PlayerData::mana),
            Codec.INT.fieldOf("max_mana").forGetter(PlayerData::maxMana),
            Codec.INT.fieldOf("stamina").forGetter(PlayerData::stamina),
            Codec.INT.fieldOf("max_stamina").forGetter(PlayerData::maxStamina),
            Codec.INT.fieldOf("strength").forGetter(PlayerData::strength),
            Codec.INT.fieldOf("defense").forGetter(PlayerData::defense),
            Codec.INT.fieldOf("magic_power").forGetter(PlayerData::magicPower),
            Codec.INT.fieldOf("magic_resistance").forGetter(PlayerData::magicResistance),
            Codec.INT.fieldOf("speed").forGetter(PlayerData::speed),
            Codec.INT.fieldOf("luck").forGetter(PlayerData::luck)
    ).apply(instance, PlayerData::new));

    public PlayerData {
        level = Math.clamp(level, 1, MAX_LEVEL);
        experience = Math.max(0, experience);
        maxMana = Math.max(1, maxMana);
        maxStamina = Math.max(1, maxStamina);
        mana = Math.clamp(mana, 0, maxMana);
        stamina = Math.clamp(stamina, 0, maxStamina);
    }

    public PlayerData withMana(int value) {
        return new PlayerData(level, experience, value, maxMana, stamina, maxStamina, strength, defense, magicPower, magicResistance, speed, luck);
    }

    public PlayerData withStamina(int value) {
        return new PlayerData(level, experience, mana, maxMana, value, maxStamina, strength, defense, magicPower, magicResistance, speed, luck);
    }

    public PlayerData regenerate(int manaAmount, int staminaAmount) {
        return withMana(mana + manaAmount).withStamina(stamina + staminaAmount);
    }

    public static int experienceForNextLevel(int level) {
        int clampedLevel = Math.clamp(level, 1, MAX_LEVEL);
        return 100 + (clampedLevel - 1) * 50;
    }

    public PlayerData addExperience(int amount) {
        if (amount <= 0 || level >= MAX_LEVEL) return this;
        int nextLevel = level;
        int nextExperience = experience + amount;
        int nextMaxMana = maxMana;
        int nextMaxStamina = maxStamina;
        int nextStrength = strength;
        int nextDefense = defense;
        int nextMagicPower = magicPower;
        int nextMagicResistance = magicResistance;
        int nextSpeed = speed;
        int nextLuck = luck;
        while (nextLevel < MAX_LEVEL && nextExperience >= experienceForNextLevel(nextLevel)) {
            nextExperience -= experienceForNextLevel(nextLevel);
            nextLevel++;
            nextMaxMana += 5;
            nextMaxStamina += 3;
            nextStrength++;
            nextDefense++;
            nextMagicPower++;
            nextMagicResistance++;
            nextSpeed++;
            nextLuck++;
        }
        if (nextLevel >= MAX_LEVEL) nextExperience = 0;
        return new PlayerData(nextLevel, nextExperience, mana, nextMaxMana, stamina, nextMaxStamina,
                nextStrength, nextDefense, nextMagicPower, nextMagicResistance, nextSpeed, nextLuck);
    }
}
