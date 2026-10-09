package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Cooldown display generated from server-provided state. */
public final class AbilityScreen extends Screen {
    private static final java.util.List<Identifier> ABILITIES = java.util.List.of(
            Identifier.fromNamespaceAndPath("rezero", "shadow_bind"), Identifier.fromNamespaceAndPath("rezero", "huma"),
            Identifier.fromNamespaceAndPath("rezero", "minya"), Identifier.fromNamespaceAndPath("rezero", "fura"),
            Identifier.fromNamespaceAndPath("rezero", "dona"), Identifier.fromNamespaceAndPath("rezero", "dodge")
    );
    public AbilityScreen() { super(Component.translatable("screen.rezero.abilities")); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        long gameTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        int x = width / 2 - 120, y = 48;
        for (Identifier ability : ABILITIES) {
            long remaining = Math.max(0L, ClientReZeroState.cooldownReadyAt(ability) - gameTime);
            String state = remaining == 0 ? "READY" : "COOLDOWN " + remaining / 20.0F + "s";
            graphics.drawString(font, ability.getPath() + ": " + state, x, y, remaining == 0 ? 0x76DC76 : 0xFFBE64);
            y += 16;
        }
    }
}
