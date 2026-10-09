package com.rezero.mod.authority;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorityDefinitionTest {
    @Test
    void clampsUnsafeCostsAndCooldowns() {
        AuthorityDefinition definition = new AuthorityDefinition("Test", "invisible_hand", -10, -1);

        assertEquals(0, definition.cooldownTicks());
        assertEquals(0, definition.manaCost());
    }
}
