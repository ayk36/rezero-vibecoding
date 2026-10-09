package com.rezero.mod.item;

import com.rezero.mod.player.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Server-authoritative mana consumable. */
public final class ManaPotionItem extends Item {
    private final int restoredMana;

    public ManaPotionItem(Properties properties, int restoredMana) {
        super(properties.stacksTo(16));
        this.restoredMana = restoredMana;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        var before = PlayerDataManager.INSTANCE.getPlayerData(serverPlayer);
        if (before.mana() >= before.maxMana()) return InteractionResult.FAIL;
        PlayerDataManager.INSTANCE.regenerate(serverPlayer, restoredMana, 0);
        if (!serverPlayer.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
