package com.rezero.mod.client.screen;

import com.rezero.mod.client.ClientReZeroState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only quest journal backed by a synchronized server summary. */
public final class QuestScreen extends Screen {
    public QuestScreen() {
        super(Component.translatable("screen.rezero.quest"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        if (ClientReZeroState.quests().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("screen.rezero.no_quests"), width / 2, height / 2, 0xAAAAAA);
            return;
        }

        int x = width / 2 - 140;
        int y = 50;
        for (var quest : ClientReZeroState.quests()) {
            graphics.drawString(font, quest.title() + " [" + quest.status() + "]", x, y, "COMPLETED".equals(quest.status()) ? 0x76DC76 : 0xFFFFFF);
            graphics.drawString(font, quest.completedObjectives() + "/" + quest.objectiveCount() + " objectives", x, y + 12, 0xAAAAAA);
            y += 34;
            if (y > height - 30) {
                break;
            }
        }
    }
}
