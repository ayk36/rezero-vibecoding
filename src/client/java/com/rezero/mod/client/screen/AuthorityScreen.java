package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only authority and divine protection snapshot. */
public final class AuthorityScreen extends Screen {
    public AuthorityScreen() {
        super(Component.translatable("screen.rezero.authority"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        int x = width / 2 - 140;
        int y = 48;
        graphics.drawString(font, Component.translatable("screen.rezero.authorities"), x, y, 0xB98CFF);
        y += 16;
        for (var authority : ClientReZeroState.authorities()) {
            graphics.drawString(font, authority.toString(), x, y, 0xE0E0E0);
            y += 14;
        }
        if (ClientReZeroState.authorities().isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.rezero.none"), x, y, 0xAAAAAA);
            y += 14;
        }
        y += 16;
        graphics.drawString(font, Component.translatable("screen.rezero.protections"), x, y, 0xFFD86B);
        y += 16;
        for (var protection : ClientReZeroState.protections()) {
            graphics.drawString(font, protection.toString(), x, y, 0xE0E0E0);
            y += 14;
        }
        if (ClientReZeroState.protections().isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.rezero.none"), x, y, 0xAAAAAA);
        }
    }
}
