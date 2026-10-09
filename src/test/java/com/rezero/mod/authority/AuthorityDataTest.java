package com.rezero.mod.authority;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityDataTest {
    @Test
    void grantsAuthorityAndProtectionIndependently() {
        var authority = ReZeroMod.id("invisible_hand");
        var protection = ReZeroMod.id("divine_protection_fire");
        AuthorityData data = AuthorityData.DEFAULT.grantAuthority(authority).grantProtection(protection);

        assertTrue(data.authorities().contains(authority));
        assertTrue(data.divineProtections().contains(protection));
    }
}
