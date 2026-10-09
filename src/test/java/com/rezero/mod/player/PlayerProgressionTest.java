package com.rezero.mod.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerProgressionTest {
    @Test
    void grantsLevelStatsAndCarriesRemainder() {
        PlayerData data = PlayerData.DEFAULT.addExperience(160);

        assertEquals(2, data.level());
        assertEquals(60, data.experience());
        assertEquals(105, data.maxMana());
        assertEquals(1, data.strength());
    }

    @Test
    void stopsExperienceAtMaximumLevel() {
        PlayerData maxed = new PlayerData(100, 0, 100, 100, 100, 100, 0, 0, 0, 0, 0, 0);

        assertEquals(maxed, maxed.addExperience(100_000));
    }
}
