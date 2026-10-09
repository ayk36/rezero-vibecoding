package com.rezero.mod.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/** Persistent accessory state. Slots are server-owned and do not replace vanilla armor slots. */
public record EquipmentData(ItemStack firstAccessory, ItemStack secondAccessory) {
    public static final EquipmentData DEFAULT = new EquipmentData(ItemStack.EMPTY, ItemStack.EMPTY);
    public static final Codec<EquipmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("first_accessory", ItemStack.EMPTY).forGetter(EquipmentData::firstAccessory),
            ItemStack.CODEC.optionalFieldOf("second_accessory", ItemStack.EMPTY).forGetter(EquipmentData::secondAccessory)
    ).apply(instance, EquipmentData::new));

    public EquipmentData {
        firstAccessory = firstAccessory.copy();
        secondAccessory = secondAccessory.copy();
    }

    public ItemStack get(AccessorySlot slot) {
        return slot == AccessorySlot.FIRST ? firstAccessory.copy() : secondAccessory.copy();
    }

    public EquipmentData with(AccessorySlot slot, ItemStack stack) {
        return slot == AccessorySlot.FIRST
                ? new EquipmentData(stack, secondAccessory)
                : new EquipmentData(firstAccessory, stack);
    }
}
