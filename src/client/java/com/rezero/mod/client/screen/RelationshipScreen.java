package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Synchronized NPC relationship display. */
public final class RelationshipScreen extends Screen {
    public RelationshipScreen() { super(Component.translatable("screen.rezero.relationship")); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        int x = width / 2 - 130;
        int y = 48;
        for (var entry : ClientReZeroState.relationships()) {
            int color = entry.value() >= 50 ? 0x76DC76 : entry.value() < 0 ? 0xE07070 : 0xE0E0E0;
            graphics.drawString(font, entry.npc() + ": " + entry.value(), x, y, color);
            y += 16;
        }
        if (ClientReZeroState.relationships().isEmpty()) graphics.drawString(font, Component.translatable("screen.rezero.none"), x, y, 0xAAAAAA);
    }
}
