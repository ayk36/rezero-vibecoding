package com.rezero.mod.registry;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.entity.WolgarmEntity;
import com.rezero.mod.entity.GreatRabbitEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Custom entity registry. All behavior remains logical-server authoritative. */
public final class ModEntities {
    public static final EntityType<WolgarmEntity> WOLGARM = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ReZeroMod.id("wolgarm"),
            FabricEntityTypeBuilder.createMob()
                    .spawnGroup(MobCategory.MONSTER)
                    .entityFactory(WolgarmEntity::new)
                    .dimensions(EntityDimensions.scalable(0.85F, 0.85F))
                    .trackRangeBlocks(64)
                    .defaultAttributes(WolgarmEntity::createAttributes)
                    .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ReZeroMod.id("wolgarm")))
    );
    public static final EntityType<GreatRabbitEntity> GREAT_RABBIT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ReZeroMod.id("great_rabbit"),
            FabricEntityTypeBuilder.createMob()
                    .spawnGroup(MobCategory.MONSTER)
                    .entityFactory(GreatRabbitEntity::new)
                    .dimensions(EntityDimensions.scalable(0.4F, 0.5F))
                    .trackRangeBlocks(48)
                    .defaultAttributes(GreatRabbitEntity::createAttributes)
                    .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ReZeroMod.id("great_rabbit")))
    );

    private ModEntities() {
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(WOLGARM, WolgarmEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GREAT_RABBIT, GreatRabbitEntity.createAttributes());
    }
}
