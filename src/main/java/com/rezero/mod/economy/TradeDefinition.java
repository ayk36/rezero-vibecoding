package com.rezero.mod.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Data-driven single-item trade definition. Costs are evaluated server-side. */
public record TradeDefinition(Identifier npc, Identifier item, int basePrice, Identifier currency, int relationshipDiscountPerTen) {
    public static final Codec<TradeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("npc").forGetter(TradeDefinition::npc),
            Identifier.CODEC.fieldOf("item").forGetter(TradeDefinition::item),
            Codec.INT.fieldOf("base_price").forGetter(TradeDefinition::basePrice),
            Identifier.CODEC.optionalFieldOf("currency", Identifier.fromNamespaceAndPath("rezero", "lugunica_copper_coin")).forGetter(TradeDefinition::currency),
            Codec.INT.optionalFieldOf("relationship_discount_per_ten", 0).forGetter(TradeDefinition::relationshipDiscountPerTen)
    ).apply(instance, TradeDefinition::new));

    public TradeDefinition {
        basePrice = Math.clamp(basePrice, 1, 1_000_000);
        relationshipDiscountPerTen = Math.clamp(relationshipDiscountPerTen, 0, 100);
    }
}
