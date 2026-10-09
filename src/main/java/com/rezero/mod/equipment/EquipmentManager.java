package com.rezero.mod.equipment;

import com.rezero.mod.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Authoritative accessory equip/unequip methods with inventory-safe transfers. */
public final class EquipmentManager {
    public static final EquipmentManager INSTANCE = new EquipmentManager();

    private EquipmentManager() {
    }

    public EquipmentData get(ServerPlayer player) {
        return player.getAttachedOrCreate(ModAttachments.EQUIPMENT_DATA, () -> EquipmentData.DEFAULT);
    }

    public boolean equipHeld(ServerPlayer player, AccessorySlot slot) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !isAccessory(held)) {
            return false;
        }
        EquipmentData data = get(player);
        ItemStack previous = data.get(slot);
        if (!previous.isEmpty() && !player.getInventory().add(previous)) {
            return false;
        }
        ItemStack equipped = held.copyWithCount(1);
        held.shrink(1);
        player.setAttached(ModAttachments.EQUIPMENT_DATA, data.with(slot, equipped));
        return true;
    }

    public boolean unequip(ServerPlayer player, AccessorySlot slot) {
        EquipmentData data = get(player);
        ItemStack equipped = data.get(slot);
        if (equipped.isEmpty() || !player.getInventory().add(equipped)) {
            return false;
        }
        player.setAttached(ModAttachments.EQUIPMENT_DATA, data.with(slot, ItemStack.EMPTY));
        return true;
    }

    public static boolean isAccessory(ItemStack stack) {
        return stack.is(com.rezero.mod.registry.ModItems.MAGIC_STONE);
    }
}
