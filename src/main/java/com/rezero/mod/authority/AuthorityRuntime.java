package com.rezero.mod.authority;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.player.PlayerDataManager;
import com.rezero.mod.registry.ModEffects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-only authority activation and passive protection enforcement. */
public final class AuthorityRuntime {
    public static final AuthorityRuntime INSTANCE = new AuthorityRuntime();
    private final Map<UUID, Map<Identifier, Long>> cooldowns = new HashMap<>();

    private AuthorityRuntime() {
    }

    public boolean activate(ServerPlayer player, Identifier authorityId, LivingEntity target) {
        AuthorityDefinition definition = AuthorityRegistry.authority(authorityId);
        if (definition == null || !AuthorityManager.INSTANCE.get(player).authorities().contains(authorityId)) {
            return false;
        }
        long gameTime = player.level().getGameTime();
        if (cooldowns.getOrDefault(player.getUUID(), Map.of()).getOrDefault(authorityId, 0L) > gameTime
                || !PlayerDataManager.INSTANCE.consumeResources(player, definition.manaCost(), 0)) {
            return false;
        }
        if ("invisible_hand".equals(definition.behavior())) {
            if (target == null || target == player || target.distanceToSqr(player) > 12.0D * 12.0D) {
                return false;
            }
            target.knockback(1.5D, player.getX() - target.getX(), player.getZ() - target.getZ());
            target.addEffect(new MobEffectInstance(ModEffects.curseHolder(), 80, 0));
        } else {
            return false;
        }
        cooldowns.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>()).put(authorityId, gameTime + definition.cooldownTicks());
        return true;
    }

    public void onTick(ServerPlayer player) {
        AuthorityData data = AuthorityManager.INSTANCE.get(player);
        for (Identifier protectionId : data.divineProtections()) {
            DivineProtectionDefinition definition = AuthorityRegistry.protection(protectionId);
            if (definition == null) continue;
            if ("fire_resistance".equals(definition.behavior()) && player.getRemainingFireTicks() > 0) {
                player.clearFire();
            }
        }
    }
}
