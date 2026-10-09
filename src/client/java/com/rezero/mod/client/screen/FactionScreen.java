package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Synchronized faction reputation display. */
public final class FactionScreen extends Screen {
    public FactionScreen() { super(Component.translatable("screen.rezero.factions")); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        int x = width / 2 - 130;
        int y = 48;
        for (var entry : ClientReZeroState.factions()) {
            int color = entry.value() >= 50 ? 0x76DC76 : entry.value() < 0 ? 0xE07070 : 0xE0E0E0;
            graphics.drawString(font, entry.faction() + ": " + entry.value(), x, y, color);
            y += 16;
        }
        if (ClientReZeroState.factions().isEmpty()) graphics.drawString(font, Component.translatable("screen.rezero.none"), x, y, 0xAAAAAA);
    }
}
