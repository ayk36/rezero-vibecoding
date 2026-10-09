package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only learned-spell list. Server decides all unlock state. */
public final class MagicScreen extends Screen {
    public MagicScreen() {
        super(Component.translatable("screen.rezero.magic"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        if (ClientReZeroState.learnedSpells().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("screen.rezero.no_spells"), width / 2, height / 2, 0xAAAAAA);
            return;
        }
        int x = width / 2 - 120;
        int y = 48;
        for (var spell : ClientReZeroState.learnedSpells()) {
            graphics.drawString(font, spell.toString(), x, y, 0xB98CFF);
            y += 18;
            if (y > height - 30) {
                break;
            }
        }
    }
}
