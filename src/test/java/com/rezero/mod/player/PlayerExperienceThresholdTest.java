package com.rezero.mod.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerExperienceThresholdTest {
    @Test
    void experienceThresholdIncreasesPerLevel() {
        assertEquals(100, PlayerData.experienceForNextLevel(1));
        assertEquals(150, PlayerData.experienceForNextLevel(2));
        assertEquals(5_050, PlayerData.experienceForNextLevel(100));
    }
}
