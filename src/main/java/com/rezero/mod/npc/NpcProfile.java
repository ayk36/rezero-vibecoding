package com.rezero.mod.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/** Data-driven profile for a named story NPC. */
public record NpcProfile(String name, String faction, boolean combat, NpcSchedule schedule, Identifier entityType, Optional<Identifier> dialogue) {
    private static final Codec<NpcSchedule> SCHEDULE_CODEC = Codec.STRING.xmap(
            raw -> NpcSchedule.valueOf(raw.toUpperCase(java.util.Locale.ROOT)),
            value -> value.name().toLowerCase(java.util.Locale.ROOT)
    );
    public static final Codec<NpcProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(NpcProfile::name),
            Codec.STRING.optionalFieldOf("faction", "rezero:neutral").forGetter(NpcProfile::faction),
            Codec.BOOL.optionalFieldOf("combat", false).forGetter(NpcProfile::combat),
            SCHEDULE_CODEC.optionalFieldOf("schedule", NpcSchedule.ALWAYS).forGetter(NpcProfile::schedule),
            Identifier.CODEC.optionalFieldOf("entity_type", Identifier.fromNamespaceAndPath("minecraft", "villager")).forGetter(NpcProfile::entityType),
            Identifier.CODEC.optionalFieldOf("dialogue").forGetter(NpcProfile::dialogue)
    ).apply(instance, NpcProfile::new));
}
