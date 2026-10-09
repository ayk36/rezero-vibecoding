package com.rezero.mod.client;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.network.payload.PlayerDataPayload;
import com.rezero.mod.network.payload.ReturnByDeathPayload;
import com.rezero.mod.network.payload.UseAbilityPayload;
import com.rezero.mod.network.payload.QuestSummaryPayload;
import com.rezero.mod.client.screen.CharacterScreen;
import com.rezero.mod.client.screen.QuestScreen;
import com.rezero.mod.client.screen.MagicScreen;
import com.rezero.mod.network.payload.MagicSummaryPayload;
import com.rezero.mod.network.payload.OpenDialoguePayload;
import com.rezero.mod.client.screen.DialogueScreen;
import com.rezero.mod.network.payload.AuthoritySummaryPayload;
import com.rezero.mod.client.screen.AuthorityScreen;
import com.rezero.mod.network.payload.RelationshipSummaryPayload;
import com.rezero.mod.client.screen.RelationshipScreen;
import com.rezero.mod.client.screen.ReturnByDeathScreen;
import com.rezero.mod.network.payload.DodgePayload;
import com.rezero.mod.network.payload.FactionSummaryPayload;
import com.rezero.mod.client.screen.FactionScreen;
import com.rezero.mod.network.payload.AbilityCooldownPayload;
import com.rezero.mod.client.screen.AbilityScreen;
import com.rezero.mod.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/** Client entrypoint: receives snapshots, captures intent, and never applies gameplay outcomes. */
public final class ReZeroClient implements ClientModInitializer {
    private static final KeyMapping.Category REZERO_CATEGORY = KeyMapping.Category.register(ReZeroMod.id("general"));
    private static final KeyMapping CHARACTER_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.character", GLFW.GLFW_KEY_R, REZERO_CATEGORY
    ));
    private static final KeyMapping MAGIC_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.magic", GLFW.GLFW_KEY_M, REZERO_CATEGORY
    ));
    private static final KeyMapping QUEST_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.quest", GLFW.GLFW_KEY_J, REZERO_CATEGORY
    ));
    private static final KeyMapping ABILITY_ONE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.ability_1", GLFW.GLFW_KEY_Z, REZERO_CATEGORY
    ));
    private static final KeyMapping DODGE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.dodge", GLFW.GLFW_KEY_LEFT_ALT, REZERO_CATEGORY
    ));
    private static final KeyMapping AUTHORITY_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.authority", GLFW.GLFW_KEY_P, REZERO_CATEGORY
    ));
    private static final KeyMapping RELATIONSHIP_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.relationship", GLFW.GLFW_KEY_O, REZERO_CATEGORY
    ));
    private static final KeyMapping FACTION_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.faction", GLFW.GLFW_KEY_I, REZERO_CATEGORY
    ));
    private static final KeyMapping ABILITY_SCREEN_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.rezero.abilities", GLFW.GLFW_KEY_K, REZERO_CATEGORY
    ));

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntities.WOLGARM, WolfRenderer::new);
        EntityRenderers.register(ModEntities.GREAT_RABBIT, RabbitRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(PlayerDataPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setPlayerData(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(ReturnByDeathPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    ClientReZeroState.setReturnByDeath(payload);
                    if (payload.restoring()) context.client().setScreen(new ReturnByDeathScreen());
                })
        );
        ClientPlayNetworking.registerGlobalReceiver(QuestSummaryPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setQuests(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(MagicSummaryPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setMagic(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(OpenDialoguePayload.TYPE, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new DialogueScreen(payload)))
        );
        ClientPlayNetworking.registerGlobalReceiver(AuthoritySummaryPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setAuthority(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(RelationshipSummaryPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setRelationships(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(FactionSummaryPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setFactions(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(AbilityCooldownPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientReZeroState.setCooldown(payload))
        );

        ClientTickEvents.END_CLIENT_TICK.register(this::handleInput);
    }

    private void handleInput(Minecraft client) {
        if (client.screen instanceof ReturnByDeathScreen && ClientReZeroState.returnByDeathFade() <= 0.0F) {
            client.setScreen(null);
        }
        while (CHARACTER_KEY.consumeClick()) {
            client.setScreen(new CharacterScreen());
        }
        while (MAGIC_KEY.consumeClick()) {
            client.setScreen(new MagicScreen());
        }
        while (QUEST_KEY.consumeClick()) {
            client.setScreen(new QuestScreen());
        }
        while (DODGE_KEY.consumeClick()) {
            ClientPlayNetworking.send(new DodgePayload());
        }
        while (AUTHORITY_KEY.consumeClick()) {
            client.setScreen(new AuthorityScreen());
        }
        while (RELATIONSHIP_KEY.consumeClick()) {
            client.setScreen(new RelationshipScreen());
        }
        while (FACTION_KEY.consumeClick()) {
            client.setScreen(new FactionScreen());
        }
        while (ABILITY_SCREEN_KEY.consumeClick()) {
            client.setScreen(new AbilityScreen());
        }
        while (ABILITY_ONE_KEY.consumeClick()) {
            sendShadowBindIntent(client);
        }
    }

    private void sendShadowBindIntent(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        int targetId = client.hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity().getId() : -1;
        var look = player.getLookAngle();
        ClientPlayNetworking.send(new UseAbilityPayload(ReZeroMod.id("shadow_bind"), targetId, (float) look.x, (float) look.y, (float) look.z));
    }
}
