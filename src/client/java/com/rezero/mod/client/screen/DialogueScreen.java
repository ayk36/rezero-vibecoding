package com.rezero.mod.client.screen;

import com.rezero.mod.network.payload.ChooseDialoguePayload;
import com.rezero.mod.network.payload.OpenDialoguePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client dialogue UI. Choice clicks communicate intent; effects remain server-side. */
public final class DialogueScreen extends Screen {
    private final OpenDialoguePayload dialogue;

    public DialogueScreen(OpenDialoguePayload dialogue) {
        super(Component.translatable("screen.rezero.dialogue"));
        this.dialogue = dialogue;
    }

    @Override
    protected void init() {
        int y = height - 82;
        for (int index = 0; index < dialogue.choices().size() && index < 4; index++) {
            var choice = dialogue.choices().get(index);
            addRenderableWidget(Button.builder(Component.literal(choice.text()), button -> {
                ClientPlayNetworking.send(new ChooseDialoguePayload(dialogue.dialogueId(), dialogue.nodeId(), choice.index()));
                onClose();
            }).bounds(width / 2 - 150, y + index * 20, 300, 18).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, Component.literal(dialogue.speaker()), width / 2, height / 2 - 72, 0xD9C3FF);
        graphics.drawWordWrap(font, Component.literal(dialogue.text()), width / 2 - 150, height / 2 - 48, 300, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
