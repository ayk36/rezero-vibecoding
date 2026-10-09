package com.rezero.mod.boss;

import com.rezero.mod.ReZeroMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.rezero.mod.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import com.rezero.mod.world.WorldEventManager;
import com.rezero.mod.world.WorldEventStatus;
import net.minecraft.world.phys.Vec3;
import com.rezero.mod.entity.WitchCultistController;
import com.rezero.mod.player.PlayerDataManager;
import com.rezero.mod.story.StoryManager;
import com.rezero.mod.registry.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-managed boss bars and phase messages for configured boss entities. */
public final class BossManager {
    private static final Map<UUID, ActiveBoss> ACTIVE = new HashMap<>();

    private BossManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> tick());
    }

    public static boolean spawn(ServerLevel level, Identifier bossId, Vec3 position) {
        return spawn(level, bossId, position, null);
    }

    public static boolean spawn(ServerLevel level, Identifier bossId, Vec3 position, Identifier worldEventId) {
        BossDefinition definition = BossRegistry.get(bossId);
        if (definition == null) {
            return false;
        }
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(definition.entityType()).orElse(null);
        if (type == null || !(type.create(level, EntitySpawnReason.COMMAND) instanceof LivingEntity entity)) {
            return false;
        }
        var maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(definition.maxHealth());
        }
        entity.setHealth(definition.maxHealth());
        entity.setPos(position);
        entity.setCustomName(Component.literal(definition.name()));
        entity.setCustomNameVisible(true);
        level.addFreshEntity(entity);
        if (worldEventId != null) entity.addTag("rezero_event:" + worldEventId);
        ACTIVE.put(entity.getUUID(), new ActiveBoss(entity, bossId, definition, new ServerBossEvent(
                Component.literal(definition.name()), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS
        ), "", worldEventId));
        return true;
    }

    private static void tick() {
        ACTIVE.values().removeIf(active -> {
            if (!active.entity().isAlive()) {
                rewardDefeat(active);
                active.bar().removeAllPlayers();
                if (active.worldEventId() != null && active.entity().level() instanceof ServerLevel level) {
                    WorldEventManager.get(level).complete(level, active.worldEventId());
                }
                return true;
            }
            float fraction = active.entity().getHealth() / active.entity().getMaxHealth();
            active.bar().setProgress(Math.clamp(fraction, 0.0F, 1.0F));
            active.bar().getPlayers().removeIf(player -> player.distanceToSqr(active.entity()) > 96.0D * 96.0D);
            for (var player : active.entity().level().players()) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && serverPlayer.distanceToSqr(active.entity()) <= 64.0D * 64.0D) {
                    active.bar().addPlayer(serverPlayer);
                }
            }
            String phase = phase(active.definition(), fraction);
            if (!phase.equals(active.phase())) {
                active.bar().setName(Component.literal(active.definition().name() + " - " + phase));
                active.setPhase(phase);
                applyPhaseEffect(active, phase);
            }
            tickMechanic(active);
            return false;
        });
    }

    private static void rewardDefeat(ActiveBoss active) {
        for (var player : active.bar().getPlayers()) {
            PlayerDataManager.INSTANCE.grantExperience(player, active.definition().experienceReward());
            if (active.definition().goldReward() > 0) {
                player.getInventory().add(new ItemStack(ModItems.LUGUNICA_GOLD_COIN, active.definition().goldReward()));
            }
            StoryManager.recordBossDefeat(player, active.bossId());
        }
    }

    private static void tickMechanic(ActiveBoss active) {
        long gameTime = active.entity().level().getGameTime();
        if (gameTime < active.nextMechanicTick()) {
            return;
        }
        String bossName = active.definition().name();
        if ("Elsa Granhiert".equals(bossName)) {
            tickElsa(active, gameTime);
        } else if ("Petelgeuse Romanée-Conti".equals(bossName)) {
            tickPetelgeuse(active, gameTime);
        } else if ("White Whale".equals(bossName)) {
            tickWhiteWhale(active, gameTime);
        }
    }

    private static void tickPetelgeuse(ActiveBoss active, long gameTime) {
        LivingEntity target = nearestPlayer(active.entity(), 32.0D);
        if (target == null) {
            active.setNextMechanicTick(gameTime + 40);
            return;
        }
        ServerLevel level = (ServerLevel) active.entity().level();
        if ("hands".equals(active.phase())) {
            // Invisible Hands are represented by server knockback/Curse, not client animation hitboxes.
            target.knockback(2.0D, active.entity().getX() - target.getX(), active.entity().getZ() - target.getZ());
            target.addEffect(new MobEffectInstance(ModEffects.curseHolder(), 120, 1));
            active.setNextMechanicTick(gameTime + 80);
        } else if ("cult".equals(active.phase())) {
            for (int index = 0; index < 3; index++) spawnCultist(level, active.entity(), index);
            active.setNextMechanicTick(gameTime + 180);
        } else {
            // Late phase teleport is bounded to a nearby valid combat position.
            active.entity().teleportTo(level, target.getX() + 3.0D, target.getY(), target.getZ() + 3.0D, java.util.Set.of(), active.entity().getYRot(), active.entity().getXRot(), false);
            target.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 60, 0));
            active.setNextMechanicTick(gameTime + 100);
        }
    }

    private static void spawnCultist(ServerLevel level, LivingEntity boss, int index) {
        var cultist = net.minecraft.world.entity.EntityType.VINDICATOR.create(level, EntitySpawnReason.EVENT);
        if (cultist == null) return;
        cultist.setPos(boss.getX() + (index - 1) * 2.0D, boss.getY(), boss.getZ() + 2.0D);
        WitchCultistController.mark(cultist);
        level.addFreshEntity(cultist);
    }

    private static void tickElsa(ActiveBoss active, long gameTime) {
        LivingEntity target = nearestPlayer(active.entity(), 18.0D);
        if (target == null) {
            active.setNextMechanicTick(gameTime + 20);
            return;
        }
        if ("berserk".equals(active.phase())) {
            // A capped reposition creates the intended sudden pressure without client-authoritative damage.
            double dx = target.getX() - active.entity().getX();
            double dz = target.getZ() - active.entity().getZ();
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            active.entity().setDeltaMovement(dx / length * 1.2D, 0.15D, dz / length * 1.2D);
            if (active.entity().distanceToSqr(target) < 9.0D) {
                target.addEffect(new MobEffectInstance(ModEffects.bleedHolder(), 120, 0));
            }
            active.setNextMechanicTick(gameTime + 40);
        } else {
            active.setNextMechanicTick(gameTime + 80);
        }
    }

    private static void tickWhiteWhale(ActiveBoss active, long gameTime) {
        ServerLevel level = (ServerLevel) active.entity().level();
        if ("fog".equals(active.phase())) {
            for (var player : active.bar().getPlayers()) {
                player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 60, 0));
            }
            active.setNextMechanicTick(gameTime + 100);
        } else if ("summon".equals(active.phase())) {
            LivingEntity target = nearestPlayer(active.entity(), 48.0D);
            if (target != null) {
                for (int index = 0; index < 2; index++) {
                    var phantom = net.minecraft.world.entity.EntityType.PHANTOM.create(level, EntitySpawnReason.EVENT);
                    if (phantom != null) {
                        phantom.setPos(active.entity().getX() + index * 2.0D, active.entity().getY(), active.entity().getZ());
                        phantom.setTarget((Player) target);
                        level.addFreshEntity(phantom);
                    }
                }
            }
            active.setNextMechanicTick(gameTime + 200);
        } else {
            for (var player : active.bar().getPlayers()) {
                player.addEffect(new MobEffectInstance(ModEffects.curseHolder(), 100, 0));
            }
            active.setNextMechanicTick(gameTime + 120);
        }
    }

    private static LivingEntity nearestPlayer(LivingEntity entity, double range) {
        return entity.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(range), player -> !player.isSpectator())
                .stream()
                .min(java.util.Comparator.comparingDouble(entity::distanceToSqr))
                .orElse(null);
    }

    private static void applyPhaseEffect(ActiveBoss active, String phase) {
        if (!"elsa".equals(active.definition().name().toLowerCase(java.util.Locale.ROOT).split(" ")[0])) {
            return;
        }
        if ("enraged".equals(phase)) {
            active.entity().addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.SPEED, 20 * 60, 0));
        } else if ("berserk".equals(phase)) {
            active.entity().addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.SPEED, 20 * 60, 1));
            for (var player : active.bar().getPlayers()) {
                player.addEffect(new MobEffectInstance(ModEffects.bleedHolder(), 100, 0));
                player.addEffect(new MobEffectInstance(ModEffects.curseHolder(), 160, 0));
            }
        }
    }

    private static String phase(BossDefinition definition, float healthFraction) {
        for (BossPhase phase : definition.phases()) {
            if (healthFraction >= phase.healthThreshold()) {
                return phase.id();
            }
        }
        return definition.phases().getLast().id();
    }

    private static final class ActiveBoss {
        private final LivingEntity entity;
        private final Identifier bossId;
        private final BossDefinition definition;
        private final ServerBossEvent bar;
        private final Identifier worldEventId;
        private String phase;
        private long nextMechanicTick;

        private ActiveBoss(LivingEntity entity, Identifier bossId, BossDefinition definition, ServerBossEvent bar, String phase, Identifier worldEventId) {
            this.entity = entity;
            this.bossId = bossId;
            this.definition = definition;
            this.bar = bar;
            this.phase = phase;
            this.worldEventId = worldEventId;
        }

        private LivingEntity entity() { return entity; }
        private Identifier bossId() { return bossId; }
        private BossDefinition definition() { return definition; }
        private ServerBossEvent bar() { return bar; }
        private String phase() { return phase; }
        private void setPhase(String phase) { this.phase = phase; }
        private long nextMechanicTick() { return nextMechanicTick; }
        private void setNextMechanicTick(long tick) { this.nextMechanicTick = tick; }
        private Identifier worldEventId() { return worldEventId; }
    }
}
