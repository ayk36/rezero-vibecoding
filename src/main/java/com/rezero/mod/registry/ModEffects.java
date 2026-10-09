package com.rezero.mod.registry;

import com.rezero.mod.ReZeroMod;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Initial combat status effects. Damage always executes on the server tick. */
public final class ModEffects {
    public static final MobEffect BLEED = register("bleed", new BleedEffect());
    public static final MobEffect CURSE = register("curse", new CurseEffect());

    private ModEffects() {
    }

    public static void initialize() {
        // Forces static registry initialization.
    }

    public static Holder<MobEffect> bleedHolder() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(BLEED);
    }

    public static Holder<MobEffect> curseHolder() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CURSE);
    }

    private static MobEffect register(String path, MobEffect effect) {
        return Registry.register(BuiltInRegistries.MOB_EFFECT, ReZeroMod.id(path), effect);
    }

    private static final class BleedEffect extends MobEffect {
        private BleedEffect() {
            super(MobEffectCategory.HARMFUL, 0x8A1212);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            entity.hurtServer(level, entity.damageSources().magic(), 1.0F + amplifier);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }
    }

    private static final class CurseEffect extends MobEffect {
        private CurseEffect() {
            super(MobEffectCategory.HARMFUL, 0x4A176D);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            if (entity.getHealth() > 1.0F) {
                entity.hurtServer(level, entity.damageSources().magic(), 0.5F + amplifier * 0.5F);
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 40 == 0;
        }
    }
}
