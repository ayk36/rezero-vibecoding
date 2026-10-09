package com.rezero.mod.returnbydeath;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/** Persistent authority state; knowledge can survive a checkpoint restoration. */
public record ReturnByDeathData(boolean unlocked, Optional<Checkpoint> checkpoint, long lastRestoreGameTime) {
    public static final ReturnByDeathData DEFAULT = new ReturnByDeathData(false, Optional.empty(), Long.MIN_VALUE);
    public static final Codec<ReturnByDeathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("unlocked", false).forGetter(ReturnByDeathData::unlocked),
            Checkpoint.CODEC.optionalFieldOf("checkpoint").forGetter(ReturnByDeathData::checkpoint),
            Codec.LONG.optionalFieldOf("last_restore_game_time", Long.MIN_VALUE).forGetter(ReturnByDeathData::lastRestoreGameTime)
    ).apply(instance, ReturnByDeathData::new));

    public ReturnByDeathData withCheckpoint(Checkpoint value) {
        return new ReturnByDeathData(unlocked, Optional.of(value), lastRestoreGameTime);
    }

    public ReturnByDeathData unlocked(boolean value) {
        return new ReturnByDeathData(value, checkpoint, lastRestoreGameTime);
    }

    public ReturnByDeathData restoredAt(long gameTime) {
        return new ReturnByDeathData(unlocked, checkpoint, gameTime);
    }
}
