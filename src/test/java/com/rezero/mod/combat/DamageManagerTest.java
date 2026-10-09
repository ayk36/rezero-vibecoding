package com.rezero.mod.combat;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.player.PlayerData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageManagerTest {
    private static final PlayerData BASE = new PlayerData(1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0);

    @Test
    void magicPowerAndMagicResistanceAffectMagicDamage() {
        PlayerData caster = new PlayerData(1, 0, 1, 1, 1, 1, 0, 0, 10, 0, 0, 0);
        PlayerData target = new PlayerData(1, 0, 1, 1, 1, 1, 0, 0, 0, 25, 0, 0);
        DamageContext context = new DamageContext(null, null, DamageType.MAGIC, 10.0F, ReZeroMod.id("test"));

        assertEquals(9.0F, DamageManager.calculate(context, caster, target), 0.001F);
    }

    @Test
    void trueDamageBypassesDefense() {
        PlayerData target = new PlayerData(1, 0, 1, 1, 1, 1, 0, 100, 0, 100, 0, 0);
        DamageContext context = new DamageContext(null, null, DamageType.TRUE, 10.0F, ReZeroMod.id("test"));

        assertEquals(10.0F, DamageManager.calculate(context, BASE, target), 0.001F);
    }
}
