package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import com.rezero.mod.network.payload.PlayerDataPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only client view of the last server-provided character snapshot. */
public final class CharacterScreen extends Screen {
    public CharacterScreen() {
        super(Component.translatable("screen.rezero.character"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        PlayerDataPayload data = ClientReZeroState.playerData();
        if (data == null) {
            graphics.drawCenteredString(font, Component.translatable("screen.rezero.waiting"), width / 2, height / 2, 0xAAAAAA);
            return;
        }

        int x = width / 2 - 110;
        int y = 52;
        line(graphics, x, y, "Level", data.level());
        int experienceToNextLevel = 100 + Math.max(0, data.level() - 1) * 50;
        graphics.drawString(font, "Experience: " + data.experience() + " / " + experienceToNextLevel, x, y + 16, 0xE0E0E0);
        graphics.drawString(font, "Mana: " + data.mana() + " / " + data.maxMana(), x, y + 40, 0x70B8FF);
        graphics.drawString(font, "Stamina: " + data.stamina() + " / " + data.maxStamina(), x, y + 56, 0x7CDB7C);
        line(graphics, x, y + 88, "Strength", data.strength());
        line(graphics, x, y + 104, "Defense", data.defense());
        line(graphics, x, y + 120, "Magic Power", data.magicPower());
        line(graphics, x, y + 136, "Magic Resistance", data.magicResistance());
        line(graphics, x, y + 152, "Speed", data.speed());
        line(graphics, x, y + 168, "Luck", data.luck());
    }

    private void line(GuiGraphics graphics, int x, int y, String label, int value) {
        graphics.drawString(font, label + ": " + value, x, y, 0xE0E0E0);
    }
}
