package com.rezero.mod.registry;

import com.rezero.mod.ReZeroMod;
import com.rezero.mod.player.PlayerData;
import com.rezero.mod.returnbydeath.ReturnByDeathData;
import com.rezero.mod.story.StoryData;
import com.rezero.mod.magic.MagicData;
import com.rezero.mod.authority.AuthorityData;
import com.rezero.mod.equipment.EquipmentData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Persistent component-like attachments for player-owned state. */
public final class ModAttachments {
    public static final AttachmentType<PlayerData> PLAYER_DATA = AttachmentRegistry.createPersistent(
            ReZeroMod.id("player_data"), PlayerData.CODEC
    );
    public static final AttachmentType<ReturnByDeathData> RETURN_BY_DEATH = AttachmentRegistry.createPersistent(
            ReZeroMod.id("return_by_death"), ReturnByDeathData.CODEC
    );
    public static final AttachmentType<StoryData> STORY_DATA = AttachmentRegistry.createPersistent(
            ReZeroMod.id("story_data"), StoryData.CODEC
    );
    public static final AttachmentType<MagicData> MAGIC_DATA = AttachmentRegistry.createPersistent(
            ReZeroMod.id("magic_data"), MagicData.CODEC
    );
    public static final AttachmentType<AuthorityData> AUTHORITY_DATA = AttachmentRegistry.createPersistent(
            ReZeroMod.id("authority_data"), AuthorityData.CODEC
    );
    public static final AttachmentType<EquipmentData> EQUIPMENT_DATA = AttachmentRegistry.createPersistent(
            ReZeroMod.id("equipment_data"), EquipmentData.CODEC
    );

    private ModAttachments() {
    }

    public static void initialize() {
        // Forces class initialization during mod bootstrap.
    }
}
