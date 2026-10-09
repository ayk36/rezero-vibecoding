package com.rezero.mod.combat;

import com.rezero.mod.player.PlayerData;
import com.rezero.mod.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Central server-authoritative damage pipeline for Re:Zero abilities and bosses. */
public final class DamageManager {
    private static final float STAT_SCALE = 0.02F;
    private static final float MAX_REDUCTION = 0.80F;

    private DamageManager() {
    }

    public static boolean apply(DamageContext context) {
        float finalDamage = calculate(context, PlayerDataManager.INSTANCE.getPlayerData(context.attacker()), targetData(context.target()));
        if (finalDamage <= 0.0F) {
            return false;
        }
        return context.target().hurtServer(context.attacker().level(), context.attacker().damageSources().magic(), finalDamage);
    }

    /** Pure calculation helper, deterministic for tests and balance tooling. */
    public static float calculate(DamageContext context, PlayerData attacker, PlayerData target) {
        float attackStat = context.type() == DamageType.MAGIC ? attacker.magicPower() : attacker.strength();
        float scaled = context.baseDamage() * (1.0F + attackStat * STAT_SCALE);
        if (context.type() == DamageType.TRUE) {
            return scaled;
        }

        float defense = context.type() == DamageType.MAGIC ? target.magicResistance() : target.defense();
        float reduction = Math.min(MAX_REDUCTION, Math.max(0.0F, defense * 0.01F));
        return Math.max(0.0F, scaled * (1.0F - reduction));
    }

    private static PlayerData targetData(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            return PlayerDataManager.INSTANCE.getPlayerData(player);
        }
        int armor = Math.max(0, target.getArmorValue());
        return new PlayerData(1, 0, 1, 1, 1, 1, 0, armor, 0, armor, 0, 0);
    }
}
