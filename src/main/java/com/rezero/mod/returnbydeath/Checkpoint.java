package com.rezero.mod.returnbydeath;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rezero.mod.player.PlayerData;
import com.rezero.mod.story.StoryData;
import com.rezero.mod.world.TrackedStoryState;
import com.rezero.mod.magic.MagicData;
import com.rezero.mod.npc.NpcSpawnRecord;
import net.minecraft.resources.Identifier;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** A bounded player checkpoint. World rollback is intentionally limited to registered story objects. */
public record Checkpoint(
        String dimension,
        double x,
        double y,
        double z,
        float yRot,
        float xRot,
        float health,
        PlayerData playerData,
        StoryData storyData,
        MagicData magicData,
        TrackedStoryState trackedStoryState,
        Map<Identifier, NpcSpawnRecord> npcSpawns,
        List<ItemStack> inventory
) {
    public static final Codec<Checkpoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(Checkpoint::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(Checkpoint::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Checkpoint::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Checkpoint::z),
            Codec.FLOAT.fieldOf("y_rot").forGetter(Checkpoint::yRot),
            Codec.FLOAT.fieldOf("x_rot").forGetter(Checkpoint::xRot),
            Codec.FLOAT.fieldOf("health").forGetter(Checkpoint::health),
            PlayerData.CODEC.fieldOf("player_data").forGetter(Checkpoint::playerData),
            StoryData.CODEC.fieldOf("story_data").forGetter(Checkpoint::storyData),
            MagicData.CODEC.fieldOf("magic_data").forGetter(Checkpoint::magicData),
            TrackedStoryState.CODEC.fieldOf("tracked_story_state").forGetter(Checkpoint::trackedStoryState),
            Codec.unboundedMap(Identifier.CODEC, NpcSpawnRecord.CODEC).optionalFieldOf("npc_spawns", Map.of()).forGetter(Checkpoint::npcSpawns),
            ItemStack.CODEC.listOf().fieldOf("inventory").forGetter(Checkpoint::inventory)
    ).apply(instance, Checkpoint::new));

    public Checkpoint {
        inventory = inventory.stream().map(ItemStack::copy).toList();
        npcSpawns = Map.copyOf(npcSpawns);
    }
}
