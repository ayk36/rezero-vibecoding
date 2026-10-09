package com.rezero.mod.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerDataTest {
    @Test
    void clampsResourcesToTheirServerDefinedBounds() {
        PlayerData data = new PlayerData(0, -50, 999, 100, -10, 50, 0, 0, 0, 0, 0, 0);

        assertEquals(1, data.level());
        assertEquals(0, data.experience());
        assertEquals(100, data.mana());
        assertEquals(0, data.stamina());
    }

    @Test
    void regenerationCannotExceedMaximumResources() {
        PlayerData data = new PlayerData(1, 0, 98, 100, 49, 50, 0, 0, 0, 0, 0, 0);

        PlayerData regenerated = data.regenerate(10, 10);

        assertEquals(100, regenerated.mana());
        assertEquals(50, regenerated.stamina());
    }
}
