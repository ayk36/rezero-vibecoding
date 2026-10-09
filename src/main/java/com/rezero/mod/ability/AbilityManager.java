package com.rezero.mod.ability;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.config.ReZeroConfig;
import com.rezero.mod.magic.MagicDataManager;
import com.rezero.mod.magic.SpellDefinition;
import com.rezero.mod.magic.SpellRegistry;
import com.rezero.mod.network.payload.UseAbilityPayload;
import com.rezero.mod.player.PlayerDataManager;
import com.rezero.mod.combat.DamageContext;
import com.rezero.mod.combat.DamageManager;
import com.rezero.mod.combat.DamageType;
import com.rezero.mod.story.StoryManager;
import com.rezero.mod.network.ReZeroNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Authoritative ability registry, validation, resource consumption, and cooldown tracking. */
public final class AbilityManager {
    public static final AbilityManager INSTANCE = new AbilityManager();
    private static final double MAX_TARGET_DISTANCE_SQUARED = 20.0D * 20.0D;
    private static final int MIN_REQUEST_INTERVAL_TICKS = 2;

    private final Map<Identifier, Ability> abilities = new HashMap<>();
    private final Map<UUID, Map<Identifier, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<Identifier, Long>> knownSpellCache = new HashMap<>();
    private final Map<UUID, Long> lastRequests = new HashMap<>();

    private AbilityManager() {
        register(new ShadowBindAbility());
        register(new HumaAbility());
        register(new MinyaAbility());
        register(new ElCuraAbility());
        register(new FuraAbility());
        register(new DonaAbility());
    }

    public void use(ServerPlayer player, UseAbilityPayload request) {
        if (!ReZeroConfig.get().enableMagic()) {
            return;
        }

        long gameTime = player.level().getGameTime();
        long previousRequest = lastRequests.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (gameTime - previousRequest < MIN_REQUEST_INTERVAL_TICKS) {
            return;
        }
        lastRequests.put(player.getUUID(), gameTime);

        Ability ability = abilities.get(request.abilityId());
        SpellDefinition definition = SpellRegistry.get(request.abilityId());
        if (ability == null || definition == null || !knowsSpellCached(player, request.abilityId(), gameTime) || isOnCooldown(player, ability, gameTime)) {
            return;
        }

        Entity target = request.targetEntityId() < 0 ? null : player.level().getEntity(request.targetEntityId());
        if (target != null && (!target.isAlive() || target.distanceToSqr(player) > MAX_TARGET_DISTANCE_SQUARED)) {
            return;
        }

        Vec3 direction = new Vec3(request.directionX(), request.directionY(), request.directionZ());
        if (!isValidDirection(direction)) {
            return;
        }

        AbilityContext context = new AbilityContext(player, target, direction.normalize());
        if (!ability.canUse(context) || !PlayerDataManager.INSTANCE.consumeResources(player, definition.manaCost(), definition.staminaCost())) {
            return;
        }

        ability.execute(context);
        StoryManager.recordAbilityUse(player, ability.id());
        long readyAt = gameTime + definition.cooldownTicks();
        cooldowns.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>()).put(ability.id(), readyAt);
        ReZeroNetworking.sendAbilityCooldown(player, ability.id(), readyAt);
    }

    public boolean dodge(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        Identifier dodge = ReZeroMod.id("dodge");
        if (isOnCooldown(player, new SimpleCooldownAbility(dodge, 20), gameTime)
                || !PlayerDataManager.INSTANCE.consumeResources(player, 0, 20)) {
            return false;
        }
        Vec3 direction = player.getLookAngle().normalize();
        player.setDeltaMovement(player.getDeltaMovement().add(direction.x * 1.15D, 0.18D, direction.z * 1.15D));
        player.hurtMarked = true;
        long readyAt = gameTime + 20;
        cooldowns.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>()).put(dodge, readyAt);
        ReZeroNetworking.sendAbilityCooldown(player, dodge, readyAt);
        return true;
    }

    private void register(Ability ability) {
        Ability previous = abilities.putIfAbsent(ability.id(), ability);
        if (previous != null) {
            throw new IllegalStateException("Duplicate Re:Zero ability: " + ability.id());
        }
    }

    private boolean isOnCooldown(ServerPlayer player, Ability ability, long gameTime) {
        return cooldowns.getOrDefault(player.getUUID(), Map.of()).getOrDefault(ability.id(), 0L) > gameTime;
    }

    private boolean knowsSpellCached(ServerPlayer player, Identifier spellId, long gameTime) {
        Map<Identifier, Long> cache = knownSpellCache.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
        Long cachedUntil = cache.get(spellId);
        if (cachedUntil != null && cachedUntil >= gameTime) {
            return true;
        }
        boolean knows = MagicDataManager.INSTANCE.knows(player, spellId);
        if (knows) {
            cache.put(spellId, gameTime + 20);
        } else {
            cache.remove(spellId);
        }
        return knows;
    }

    private static boolean isValidDirection(Vec3 direction) {
        return Double.isFinite(direction.x) && Double.isFinite(direction.y) && Double.isFinite(direction.z)
                && direction.lengthSqr() >= 0.25D && direction.lengthSqr() <= 4.0D;
    }

    private record SimpleCooldownAbility(Identifier id, int cooldownTicks) implements Ability {
        @Override public int manaCost() { return 0; }
        @Override public int staminaCost() { return 0; }
        @Override public boolean canUse(AbilityContext context) { return true; }
        @Override public void execute(AbilityContext context) { }
    }

    /** First vertical-slice spell. Its effects are exclusively calculated on the server. */
    private static final class ShadowBindAbility implements Ability {
        private static final Identifier ID = ReZeroMod.id("shadow_bind");

        @Override
        public Identifier id() {
            return ID;
        }

        @Override
        public int manaCost() {
            return 20;
        }

        @Override
        public int staminaCost() {
            return 0;
        }

        @Override
        public int cooldownTicks() {
            return 40;
        }

        @Override
        public boolean canUse(AbilityContext context) {
            return context.target() instanceof LivingEntity living && living != context.caster();
        }

        @Override
        public void execute(AbilityContext context) {
            ((LivingEntity) context.target()).addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
        }
    }

    /** Basic fire projectile surrogate: server damage, no client-defined hit result. */
    private static final class HumaAbility implements Ability {
        private static final Identifier ID = ReZeroMod.id("huma");

        @Override
        public Identifier id() {
            return ID;
        }

        @Override
        public int manaCost() {
            return 15;
        }

        @Override
        public int staminaCost() {
            return 0;
        }

        @Override
        public int cooldownTicks() {
            return 20;
        }

        @Override
        public boolean canUse(AbilityContext context) {
            return context.target() instanceof LivingEntity living && living != context.caster();
        }

        @Override
        public void execute(AbilityContext context) {
            LivingEntity target = (LivingEntity) context.target();
            DamageManager.apply(new DamageContext(context.caster(), target, DamageType.MAGIC, 6.0F, ID));
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 60));
        }
    }

    /** Yin damage spell intended as the first direct-damage combat integration point. */
    private static final class MinyaAbility implements Ability {
        private static final Identifier ID = ReZeroMod.id("minya");

        @Override
        public Identifier id() {
            return ID;
        }

        @Override
        public int manaCost() {
            return 25;
        }

        @Override
        public int staminaCost() {
            return 0;
        }

        @Override
        public int cooldownTicks() {
            return 40;
        }

        @Override
        public boolean canUse(AbilityContext context) {
            return context.target() instanceof LivingEntity living && living != context.caster();
        }

        @Override
        public void execute(AbilityContext context) {
            DamageManager.apply(new DamageContext(context.caster(), (LivingEntity) context.target(), DamageType.MAGIC, 10.0F, ID));
        }
    }

    /** Basic Yang healing spell, constrained to the caster in the first combat slice. */
    private static final class ElCuraAbility implements Ability {
        private static final Identifier ID = ReZeroMod.id("el_cura");

        @Override
        public Identifier id() {
            return ID;
        }

        @Override
        public int manaCost() {
            return 18;
        }

        @Override
        public int staminaCost() {
            return 0;
        }

        @Override
        public int cooldownTicks() {
            return 60;
        }

        @Override
        public boolean canUse(AbilityContext context) {
            return context.target() == null;
        }

        @Override
        public void execute(AbilityContext context) {
            context.caster().heal(6.0F);
        }
    }

    /** Wind spell: pushes targets away through a server-controlled vector. */
    private static final class FuraAbility implements Ability {
        private static final Identifier ID = ReZeroMod.id("fura");
        @Override public Identifier id() { return ID; }
        @Override public int manaCost() { return 14; }
        @Override public int staminaCost() { return 0; }
        @Override public int cooldownTicks() { return 30; }
        @Override public boolean canUse(AbilityContext context) { return context.target() instanceof LivingEntity living && living != context.caster(); }
        @Override public void execute(AbilityContext context) {
            LivingEntity target = (LivingEntity) context.target();
            target.knockback(1.8D, context.caster().getX() - target.getX(), context.caster().getZ() - target.getZ());
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        }
    }

    /** Earth spell: roots the target with server-side Slowness and damage. */
    private static final class DonaAbility implements Ability {
        private static final Identifier ID = ReZeroMod.id("dona");
        @Override public Identifier id() { return ID; }
        @Override public int manaCost() { return 22; }
        @Override public int staminaCost() { return 0; }
        @Override public int cooldownTicks() { return 50; }
        @Override public boolean canUse(AbilityContext context) { return context.target() instanceof LivingEntity living && living != context.caster(); }
        @Override public void execute(AbilityContext context) {
            LivingEntity target = (LivingEntity) context.target();
            DamageManager.apply(new DamageContext(context.caster(), target, DamageType.MAGIC, 8.0F, ID));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 4));
        }
    }
}
