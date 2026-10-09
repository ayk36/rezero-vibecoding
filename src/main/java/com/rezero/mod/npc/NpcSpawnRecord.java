package com.rezero.mod.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persistent authored NPC placement. Entity UUID is only a loaded-instance hint, never authority. */
public record NpcSpawnRecord(String dimension, double x, double y, double z, float yRot, float xRot) {
    public static final Codec<NpcSpawnRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(NpcSpawnRecord::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(NpcSpawnRecord::x),
            Codec.DOUBLE.fieldOf("y").forGetter(NpcSpawnRecord::y),
            Codec.DOUBLE.fieldOf("z").forGetter(NpcSpawnRecord::z),
            Codec.FLOAT.optionalFieldOf("y_rot", 0.0F).forGetter(NpcSpawnRecord::yRot),
            Codec.FLOAT.optionalFieldOf("x_rot", 0.0F).forGetter(NpcSpawnRecord::xRot)
    ).apply(instance, NpcSpawnRecord::new));
}
