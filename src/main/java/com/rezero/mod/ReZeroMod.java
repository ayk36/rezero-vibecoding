package com.rezero.mod;

import com.mojang.logging.LogUtils;
import com.rezero.mod.command.ReZeroCommands;
import com.rezero.mod.config.ReZeroConfig;
import com.rezero.mod.network.ReZeroNetworking;
import com.rezero.mod.magic.SpellRegistry;
import com.rezero.mod.npc.NpcProfileLoader;
import com.rezero.mod.npc.StoryNpcManager;
import com.rezero.mod.player.PlayerDataEvents;
import com.rezero.mod.registry.ModAttachments;
import com.rezero.mod.registry.ModItems;
import com.rezero.mod.registry.ModEffects;
import com.rezero.mod.registry.ModEntities;
import com.rezero.mod.entity.WitchbeastSpawnController;
import com.rezero.mod.world.WorldEventRegistry;
import com.rezero.mod.world.WorldEventController;
import com.rezero.mod.authority.AuthorityRegistry;
import com.rezero.mod.economy.TradeRegistry;
import com.rezero.mod.entity.WitchCultistController;
import com.rezero.mod.entity.GreatRabbitPopulationController;
import com.rezero.mod.save.MigrationHandler;
import com.rezero.mod.returnbydeath.ReturnByDeathManager;
import com.rezero.mod.story.StoryDataLoader;
import com.rezero.mod.story.QuestEvents;
import com.rezero.mod.boss.BossRegistry;
import com.rezero.mod.boss.BossManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/** Common entrypoint. All game state is owned and updated by the logical server. */
public final class ReZeroMod implements ModInitializer {
    public static final String MOD_ID = "rezero";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ReZeroConfig.load();
        ModAttachments.initialize();
        ModItems.initialize();
        ModEffects.initialize();
        ModEntities.initialize();
        WitchbeastSpawnController.register();
        WorldEventRegistry.register();
        WorldEventController.register();
        AuthorityRegistry.register();
        TradeRegistry.register();
        WitchCultistController.register();
        GreatRabbitPopulationController.register();
        MigrationHandler.initialize();
        ReZeroNetworking.registerCommon();
        SpellRegistry.register();
        BossRegistry.register();
        BossManager.register();
        PlayerDataEvents.register();
        ReturnByDeathManager.register();
        StoryDataLoader.register();
        QuestEvents.register();
        NpcProfileLoader.register();
        StoryNpcManager.register();
        ReZeroCommands.register();
        LOGGER.info("[ReZero] Core systems initialized.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
