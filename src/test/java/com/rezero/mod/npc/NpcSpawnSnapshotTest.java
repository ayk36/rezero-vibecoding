package com.rezero.mod.npc;

import com.rezero.mod.ReZeroMod;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NpcSpawnSnapshotTest {
    @Test
    void snapshotDataRetainsProfilePlacement() {
        var profile = ReZeroMod.id("emilia");
        NpcSpawnRecord record = new NpcSpawnRecord("minecraft:overworld", 12.0D, 70.0D, -4.0D, 45.0F, 0.0F);
        Map<net.minecraft.resources.Identifier, NpcSpawnRecord> snapshot = Map.of(profile, record);

        assertEquals(record, snapshot.get(profile));
    }
}
