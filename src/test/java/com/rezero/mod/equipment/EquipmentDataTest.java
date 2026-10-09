package com.rezero.mod.equipment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentDataTest {
    @Test
    void keepsSlotsIndependent() {
        assertEquals(AccessorySlot.FIRST, AccessorySlot.valueOf("FIRST"));
        assertEquals(AccessorySlot.SECOND, AccessorySlot.values()[1]);
    }
}
