package com.rezero.mod.economy;

import com.rezero.mod.story.StoryDataManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Atomic server-side trade execution. Client never sets price, item, or reward. */
public final class TradeManager {
    private TradeManager() { }

    public static boolean purchase(ServerPlayer player, Identifier tradeId) {
        TradeDefinition trade = TradeRegistry.get(tradeId);
        if (trade == null) return false;
        var item = BuiltInRegistries.ITEM.getOptional(trade.item()).orElse(null);
        var currency = BuiltInRegistries.ITEM.getOptional(trade.currency()).orElse(null);
        if (item == null || currency == null) return false;
        int relationship = StoryDataManager.INSTANCE.get(player).relationships().getOrDefault(trade.npc(), 0);
        int cost = Math.max(1, trade.basePrice() - Math.max(0, relationship / 10) * trade.relationshipDiscountPerTen());
        if (!player.getInventory().contains(new ItemStack(currency, cost))) return false;
        ItemStack reward = new ItemStack(item);
        if (!player.getInventory().add(reward)) return false;
        if (!removeCurrency(player, currency, cost)) {
            player.getInventory().removeItem(reward);
            return false;
        }
        return true;
    }

    private static boolean removeCurrency(ServerPlayer player, net.minecraft.world.item.Item currency, int amount) {
        int remaining = amount;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(currency)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);
        }
        return remaining == 0;
    }
}
