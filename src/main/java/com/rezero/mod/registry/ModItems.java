package com.rezero.mod.registry;

import com.rezero.mod.ReZeroMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import com.rezero.mod.item.ManaPotionItem;

/** Early economy/content registry. Models and loot integration can use these stable IDs now. */
public final class ModItems {
    public static final Item LUGUNICA_COPPER_COIN = register("lugunica_copper_coin", new Item(new Item.Properties()));
    public static final Item LUGUNICA_SILVER_COIN = register("lugunica_silver_coin", new Item(new Item.Properties()));
    public static final Item LUGUNICA_GOLD_COIN = register("lugunica_gold_coin", new Item(new Item.Properties()));
    public static final Item MAGIC_STONE = register("magic_stone", new Item(new Item.Properties()));
    public static final Item MANA_POTION = register("mana_potion", new ManaPotionItem(new Item.Properties(), 40));

    private ModItems() {
    }

    public static void initialize() {
        // Forces static registry initialization.
    }

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ReZeroMod.id(path), item);
    }
}
