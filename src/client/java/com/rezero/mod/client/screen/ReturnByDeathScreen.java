package com.rezero.mod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Transient client-only transition screen; server owns restoration results. */
public final class ReturnByDeathScreen extends Screen {
    public ReturnByDeathScreen() { super(Component.translatable("screen.rezero.return_by_death")); }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xEE08000D);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 8, 0xC68BFF);
        graphics.drawCenteredString(font, Component.translatable("screen.rezero.returning"), width / 2, height / 2 + 14, 0xE0E0E0);
    }
}
