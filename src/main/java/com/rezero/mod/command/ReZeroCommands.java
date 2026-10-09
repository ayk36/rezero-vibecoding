package com.rezero.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.rezero.mod.player.PlayerData;
import com.rezero.mod.player.PlayerDataManager;
import com.rezero.mod.returnbydeath.ReturnByDeathManager;
import com.rezero.mod.story.StoryManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.resources.Identifier;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import com.rezero.mod.magic.MagicDataManager;
import com.rezero.mod.magic.SpellRegistry;
import com.rezero.mod.authority.AuthorityManager;
import com.rezero.mod.boss.BossManager;
import com.rezero.mod.world.StoryLocations;
import com.rezero.mod.registry.ModEntities;
import com.rezero.mod.world.WorldEventManager;
import com.rezero.mod.world.WorldEventStatus;
import com.rezero.mod.world.WorldEventRegistry;
import com.rezero.mod.equipment.AccessorySlot;
import com.rezero.mod.equipment.EquipmentManager;
import com.rezero.mod.world.StoryLandmarkBuilder;
import com.rezero.mod.world.TrackedStoryBlockManager;
import com.rezero.mod.economy.TradeManager;
import com.rezero.mod.entity.WitchCultistController;
import com.rezero.mod.entity.GreatRabbitPopulationController;
import com.rezero.mod.npc.NpcSpawnManager;
import com.rezero.mod.npc.NpcSpawnRecord;
import com.rezero.mod.npc.NpcProfileLoader;

/** Minimal operational commands for validating core state during development. */
public final class ReZeroCommands {
    private ReZeroCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rezero")
                .then(Commands.literal("stats").executes(context -> showStats(context.getSource())))
                .then(Commands.literal("checkpoint")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("set").executes(context -> createCheckpoint(context.getSource()))))
                .then(Commands.literal("quest")
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> startQuest(context.getSource(), StringArgumentType.getString(context, "id")))))
                        .then(Commands.literal("complete")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> completeQuest(context.getSource(), StringArgumentType.getString(context, "id"))))))
                        .then(Commands.literal("reset")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> resetQuest(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("dialogue")
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> startDialogue(context.getSource(), StringArgumentType.getString(context, "id")))))
                        .then(Commands.literal("choose")
                                .then(Commands.argument("dialogue", StringArgumentType.word())
                                        .then(Commands.argument("node", StringArgumentType.word())
                                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                        .executes(context -> chooseDialogue(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "dialogue"),
                                                                StringArgumentType.getString(context, "node"),
                                                                IntegerArgumentType.getInteger(context, "index")
                                                        )))))))
                .then(Commands.literal("npc")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .executes(context -> spawnNpc(context.getSource(), StringArgumentType.getString(context, "profile")))
                                        .then(Commands.argument("position", Vec3Argument.vec3())
                                                .executes(context -> spawnNpc(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "profile"),
                                                        Vec3Argument.getVec3(context, "position")
                                                )))))
                        .then(Commands.literal("restore")
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .executes(context -> restoreNpc(context.getSource(), StringArgumentType.getString(context, "profile"))))))
                .then(Commands.literal("spawn")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("wolgarm").executes(context -> spawnWolgarm(context.getSource()))))
                .then(Commands.literal("rabbit")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> spawnGreatRabbit(context.getSource())))
                .then(Commands.literal("rabbit_swarm")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> startRabbitSwarm(context.getSource())))
                .then(Commands.literal("cultist")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> spawnCultist(context.getSource())))
                .then(Commands.literal("story")
                        .then(Commands.literal("flags").executes(context -> showStoryFlags(context.getSource())))
                        .then(Commands.literal("relationship")
                                .then(Commands.argument("npc", StringArgumentType.word())
                                        .executes(context -> showRelationship(context.getSource(), StringArgumentType.getString(context, "npc"))))))
                .then(Commands.literal("magic")
                        .then(Commands.literal("learn")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .executes(context -> learnSpell(context.getSource(), StringArgumentType.getString(context, "spell")))))
                        .then(Commands.literal("list").executes(context -> listSpells(context.getSource()))))
                .then(Commands.literal("equipment")
                        .then(Commands.literal("equip")
                                .then(Commands.argument("slot", StringArgumentType.word())
                                        .executes(context -> equipAccessory(context.getSource(), StringArgumentType.getString(context, "slot")))))
                        .then(Commands.literal("unequip")
                                .then(Commands.argument("slot", StringArgumentType.word())
                                        .executes(context -> unequipAccessory(context.getSource(), StringArgumentType.getString(context, "slot"))))))
                .then(Commands.literal("trade")
                        .then(Commands.literal("buy")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> buyTrade(context.getSource(), StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("authority")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("grant")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> grantAuthority(context.getSource(), StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("divine")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("grant")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> grantProtection(context.getSource(), StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("world")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("set")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(context -> setTrackedWorldValue(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "key"),
                                                        StringArgumentType.getString(context, "value")
                                                ))))))
                        .then(Commands.literal("mansion")
                                .then(Commands.literal("locate").executes(context -> locateMansion(context.getSource()))))
                        .then(Commands.literal("sanctuary")
                                .then(Commands.literal("locate").executes(context -> locateLandmark(context.getSource(), "sanctuary"))))
                        .then(Commands.literal("capital")
                                .then(Commands.literal("locate").executes(context -> locateLandmark(context.getSource(), "capital"))))
                        .then(Commands.literal("build")
                                .then(Commands.argument("landmark", StringArgumentType.word())
                                        .executes(context -> buildLandmark(context.getSource(), StringArgumentType.getString(context, "landmark")))))
                        .then(Commands.literal("track_block")
                                .executes(context -> trackStoryBlock(context.getSource())))
                .then(Commands.literal("boss")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("id", StringArgumentType.word())
                                                .executes(context -> spawnBoss(context.getSource(), StringArgumentType.getString(context, "id")))
                                        .then(Commands.argument("position", Vec3Argument.vec3())
                                                .executes(context -> spawnBoss(context.getSource(), StringArgumentType.getString(context, "id"), Vec3Argument.getVec3(context, "position")))))))
                .then(Commands.literal("event")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("set")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("status", StringArgumentType.word())
                                                .executes(context -> setWorldEvent(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        StringArgumentType.getString(context, "status")
                                                ))))))
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> startWorldEvent(context.getSource(), StringArgumentType.getString(context, "id")))))
                        .then(Commands.literal("fail")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> failWorldEvent(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("debug")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("player").executes(context -> showDebugPlayer(context.getSource()))))
        );
    }

    private static int showStats(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command requires a player."));
            return 0;
        }

        PlayerData data = PlayerDataManager.INSTANCE.getPlayerData(player);
        source.sendSuccess(() -> Component.translatable(
                "command.rezero.stats", data.level(), data.mana(), data.maxMana(), data.stamina(), data.maxStamina()
        ), false);
        return 1;
    }

    private static int createCheckpoint(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command requires a player."));
            return 0;
        }

        ReturnByDeathManager.createCheckpoint(player);
        return 1;
    }

    private static int showDebugPlayer(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command requires a player."));
            return 0;
        }

        var returnByDeath = ReturnByDeathManager.get(player);
        source.sendSuccess(() -> Component.literal(
                "Re:Zero debug: RBD unlocked=" + returnByDeath.unlocked()
                        + ", checkpoint=" + returnByDeath.checkpoint().isPresent()
        ), false);
        return 1;
    }

    private static int startQuest(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        Identifier id = Identifier.tryParse(rawId);
        return id != null && StoryManager.startQuest(player, id) ? 1 : failure(source, "Quest could not be started.");
    }

    private static int completeQuest(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        Identifier id = Identifier.tryParse(rawId);
        return id != null && StoryManager.completeQuest(player, id) ? 1 : failure(source, "Quest could not be completed.");
    }

    private static int resetQuest(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        Identifier id = Identifier.tryParse(rawId);
        if (player == null || id == null || !com.rezero.mod.story.StoryDataManager.INSTANCE.resetQuest(player, id)) {
            return player == null ? 0 : failure(source, "Quest could not be reset.");
        }
        source.sendSuccess(() -> Component.literal("Quest reset: " + id), false);
        return 1;
    }

    private static int startDialogue(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        Identifier id = Identifier.tryParse(rawId);
        return id != null && StoryManager.beginDialogue(player, id) ? 1 : failure(source, "Dialogue is unavailable.");
    }

    private static int chooseDialogue(CommandSourceStack source, String rawDialogueId, String node, int index) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        Identifier id = Identifier.tryParse(rawDialogueId);
        return id != null && StoryManager.choose(player, id, node, index) ? 1 : failure(source, "Dialogue choice is invalid.");
    }

    private static int spawnNpc(CommandSourceStack source, String rawProfile) {
        return spawnNpc(source, rawProfile, source.getPosition());
    }

    private static int spawnNpc(CommandSourceStack source, String rawProfile, net.minecraft.world.phys.Vec3 position) {
        Identifier profileId = Identifier.tryParse(rawProfile);
        if (profileId == null || NpcProfileLoader.get(profileId) == null) {
            return failure(source, "NPC profile is unavailable.");
        }
        ServerLevel level = source.getLevel();
        if (!NpcSpawnManager.get(level).recordAndSpawn(level, profileId, new NpcSpawnRecord(
                level.dimension().identifier().toString(), position.x, position.y, position.z, source.getRotation().y, source.getRotation().x
        ))) return failure(source, "NPC could not be created.");
        source.sendSuccess(() -> Component.literal("Spawned story NPC: " + profileId), false);
        return 1;
    }

    private static int restoreNpc(CommandSourceStack source, String rawProfile) {
        Identifier profileId = Identifier.tryParse(rawProfile);
        if (profileId == null || !NpcSpawnManager.get(source.getLevel()).restore(source.getLevel(), profileId)) {
            return failure(source, "No matching NPC spawn record in this dimension.");
        }
        source.sendSuccess(() -> Component.literal("Restored story NPC: " + profileId), false);
        return 1;
    }

    private static int showStoryFlags(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        var flags = com.rezero.mod.story.StoryDataManager.INSTANCE.get(player).flags();
        source.sendSuccess(() -> Component.literal("Story flags: " + (flags.isEmpty() ? "none" : String.join(", ", flags))), false);
        return 1;
    }

    private static int showRelationship(CommandSourceStack source, String rawNpcId) {
        ServerPlayer player = requirePlayer(source);
        Identifier npcId = Identifier.tryParse(rawNpcId);
        if (player == null || npcId == null) {
            return player == null ? 0 : failure(source, "Invalid NPC identifier.");
        }
        int relationship = com.rezero.mod.story.StoryDataManager.INSTANCE.get(player).relationships().getOrDefault(npcId, 0);
        source.sendSuccess(() -> Component.literal("Relationship with " + npcId + ": " + relationship), false);
        return 1;
    }

    private static int learnSpell(CommandSourceStack source, String rawSpellId) {
        ServerPlayer player = requirePlayer(source);
        Identifier spellId = Identifier.tryParse(rawSpellId);
        if (player == null || spellId == null || SpellRegistry.get(spellId) == null) {
            return player == null ? 0 : failure(source, "Spell definition is unavailable.");
        }
        if (!MagicDataManager.INSTANCE.learn(player, spellId)) {
            return failure(source, "Spell is already learned.");
        }
        source.sendSuccess(() -> Component.literal("Learned spell: " + spellId), false);
        return 1;
    }

    private static int listSpells(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        var spells = MagicDataManager.INSTANCE.get(player).learnedSpells().stream().sorted(java.util.Comparator.comparing(Identifier::toString)).map(Identifier::toString).toList();
        source.sendSuccess(() -> Component.literal("Learned spells: " + (spells.isEmpty() ? "none" : String.join(", ", spells))), false);
        return 1;
    }

    private static int grantAuthority(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        Identifier id = Identifier.tryParse(rawId);
        if (player == null || id == null || !id.getNamespace().equals(com.rezero.mod.ReZeroMod.MOD_ID)) {
            return player == null ? 0 : failure(source, "Invalid Re:Zero authority identifier.");
        }
        if (!AuthorityManager.INSTANCE.grantAuthority(player, id)) {
            return failure(source, "Authority is already granted.");
        }
        source.sendSuccess(() -> Component.literal("Granted authority: " + id), false);
        return 1;
    }

    private static int grantProtection(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        Identifier id = Identifier.tryParse(rawId);
        if (player == null || id == null || !id.getNamespace().equals(com.rezero.mod.ReZeroMod.MOD_ID)) {
            return player == null ? 0 : failure(source, "Invalid Re:Zero divine protection identifier.");
        }
        if (!AuthorityManager.INSTANCE.grantProtection(player, id)) {
            return failure(source, "Divine protection is already granted.");
        }
        source.sendSuccess(() -> Component.literal("Granted divine protection: " + id), false);
        return 1;
    }

    private static int setTrackedWorldValue(CommandSourceStack source, String key, String value) {
        com.rezero.mod.world.TrackedStoryStateManager.get(source.getLevel()).set(key, value);
        source.sendSuccess(() -> Component.literal("Tracked story state updated: " + key), false);
        return 1;
    }

    private static int spawnBoss(CommandSourceStack source, String rawId) {
        return spawnBoss(source, rawId, source.getPosition());
    }

    private static int spawnBoss(CommandSourceStack source, String rawId, net.minecraft.world.phys.Vec3 position) {
        Identifier id = Identifier.tryParse(rawId);
        if (id == null || !BossManager.spawn(source.getLevel(), id, position)) {
            return failure(source, "Boss definition or entity type is unavailable.");
        }
        source.sendSuccess(() -> Component.literal("Spawned boss: " + id), false);
        return 1;
    }

    private static int locateMansion(CommandSourceStack source) {
        var position = source.getPosition();
        boolean registered = StoryLocations.locateMansion(source.getLevel(), net.minecraft.core.BlockPos.containing(position.x, position.y, position.z));
        if (!registered) {
            return failure(source, "Roswaal Mansion landmark is already registered in this dimension.");
        }
        source.sendSuccess(() -> Component.literal("Roswaal Mansion story landmark registered."), false);
        return 1;
    }

    private static int locateLandmark(CommandSourceStack source, String landmark) {
        var position = source.getPosition();
        var blockPos = net.minecraft.core.BlockPos.containing(position.x, position.y, position.z);
        boolean registered = switch (landmark) {
            case "sanctuary" -> StoryLocations.locateSanctuary(source.getLevel(), blockPos);
            case "capital" -> StoryLocations.locateRoyalCapital(source.getLevel(), blockPos);
            default -> false;
        };
        if (!registered) return failure(source, "Story landmark is already registered in this dimension.");
        source.sendSuccess(() -> Component.literal("Story landmark registered: " + landmark), false);
        return 1;
    }

    private static int buildLandmark(CommandSourceStack source, String landmark) {
        var position = source.getPosition();
        var blockPos = net.minecraft.core.BlockPos.containing(position.x, position.y, position.z);
        boolean built = switch (landmark.toLowerCase(java.util.Locale.ROOT)) {
            case "mansion" -> StoryLandmarkBuilder.buildMansionMarker(source.getLevel(), blockPos);
            case "sanctuary" -> StoryLandmarkBuilder.buildSanctuaryMarker(source.getLevel(), blockPos);
            case "capital" -> StoryLandmarkBuilder.buildCapitalMarker(source.getLevel(), blockPos);
            default -> false;
        };
        if (!built) return failure(source, "Landmark could not be built here. Use mansion, sanctuary, or capital on clear ground.");
        source.sendSuccess(() -> Component.literal("Development landmark built: " + landmark), false);
        return 1;
    }

    private static int trackStoryBlock(CommandSourceStack source) {
        var position = source.getPosition();
        var blockPos = net.minecraft.core.BlockPos.containing(position.x, position.y - 1.0D, position.z);
        TrackedStoryBlockManager.track(source.getLevel(), blockPos);
        source.sendSuccess(() -> Component.literal("Tracked story block: " + blockPos), false);
        return 1;
    }

    private static int spawnWolgarm(CommandSourceStack source) {
        var wolgarm = ModEntities.WOLGARM.create(source.getLevel(), EntitySpawnReason.COMMAND);
        if (wolgarm == null) {
            return failure(source, "Wolgarm could not be created.");
        }
        var position = source.getPosition();
        wolgarm.setPos(position.x, position.y, position.z);
        source.getLevel().addFreshEntity(wolgarm);
        source.sendSuccess(() -> Component.literal("Spawned Wolgarm."), false);
        return 1;
    }

    private static int spawnGreatRabbit(CommandSourceStack source) {
        var rabbit = ModEntities.GREAT_RABBIT.create(source.getLevel(), EntitySpawnReason.COMMAND);
        if (rabbit == null) {
            return failure(source, "Great Rabbit could not be created.");
        }
        var position = source.getPosition();
        rabbit.setPos(position.x, position.y, position.z);
        source.getLevel().addFreshEntity(rabbit);
        source.sendSuccess(() -> Component.literal("Spawned Great Rabbit member."), false);
        return 1;
    }

    private static int spawnCultist(CommandSourceStack source) {
        var cultist = net.minecraft.world.entity.EntityType.VINDICATOR.create(source.getLevel(), EntitySpawnReason.COMMAND);
        if (cultist == null) return failure(source, "Witch Cultist could not be created.");
        var position = source.getPosition();
        cultist.setPos(position.x, position.y, position.z);
        WitchCultistController.mark(cultist);
        source.getLevel().addFreshEntity(cultist);
        source.sendSuccess(() -> Component.literal("Spawned Witch Cultist."), false);
        return 1;
    }

    private static int startRabbitSwarm(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null || !GreatRabbitPopulationController.start(source.getLevel(), player)) {
            return player == null ? 0 : failure(source, "A Great Rabbit swarm is already active nearby.");
        }
        source.sendSuccess(() -> Component.literal("Great Rabbit swarm started."), false);
        return 1;
    }

    private static int setWorldEvent(CommandSourceStack source, String rawId, String rawStatus) {
        Identifier eventId = Identifier.tryParse(rawId);
        WorldEventStatus status;
        try {
            status = WorldEventStatus.valueOf(rawStatus.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return failure(source, "Invalid event status.");
        }
        if (eventId == null) {
            return failure(source, "Invalid event identifier.");
        }
        WorldEventManager.get(source.getLevel()).set(eventId, status);
        source.sendSuccess(() -> Component.literal("World event " + eventId + " set to " + status), false);
        return 1;
    }

    private static int startWorldEvent(CommandSourceStack source, String rawId) {
        Identifier eventId = Identifier.tryParse(rawId);
        var definition = eventId == null ? null : WorldEventRegistry.get(eventId);
        if (definition == null) {
            return failure(source, "World event definition is unavailable.");
        }
        var manager = WorldEventManager.get(source.getLevel());
        if (!manager.transition(eventId, WorldEventStatus.AVAILABLE, WorldEventStatus.ACTIVE)
                && !manager.transition(eventId, WorldEventStatus.INACTIVE, WorldEventStatus.ACTIVE)) {
            return failure(source, "World event is not startable in its current state.");
        }
        if (!BossManager.spawn(source.getLevel(), definition.boss(), source.getPosition(), eventId)) {
            manager.set(eventId, WorldEventStatus.FAILED);
            return failure(source, "World event boss could not be spawned.");
        }
        source.sendSuccess(() -> Component.literal("World event started: " + eventId), false);
        return 1;
    }

    private static int failWorldEvent(CommandSourceStack source, String rawId) {
        Identifier eventId = Identifier.tryParse(rawId);
        if (eventId == null || WorldEventRegistry.get(eventId) == null) return failure(source, "World event definition is unavailable.");
        WorldEventManager.get(source.getLevel()).failAndCleanup(source.getLevel(), eventId);
        source.sendSuccess(() -> Component.literal("World event failed and cleaned up: " + eventId), false);
        return 1;
    }

    private static int equipAccessory(CommandSourceStack source, String rawSlot) {
        ServerPlayer player = requirePlayer(source);
        AccessorySlot slot = accessorySlot(rawSlot);
        if (player == null || slot == null || !EquipmentManager.INSTANCE.equipHeld(player, slot)) {
            return player == null ? 0 : failure(source, "Hold a Magic Stone and use slot first or second.");
        }
        source.sendSuccess(() -> Component.literal("Accessory equipped: " + rawSlot), false);
        return 1;
    }

    private static int unequipAccessory(CommandSourceStack source, String rawSlot) {
        ServerPlayer player = requirePlayer(source);
        AccessorySlot slot = accessorySlot(rawSlot);
        if (player == null || slot == null || !EquipmentManager.INSTANCE.unequip(player, slot)) {
            return player == null ? 0 : failure(source, "Accessory slot is empty or inventory is full.");
        }
        source.sendSuccess(() -> Component.literal("Accessory unequipped: " + rawSlot), false);
        return 1;
    }

    private static AccessorySlot accessorySlot(String rawSlot) {
        return switch (rawSlot.toLowerCase(java.util.Locale.ROOT)) {
            case "first", "1" -> AccessorySlot.FIRST;
            case "second", "2" -> AccessorySlot.SECOND;
            default -> null;
        };
    }

    private static int buyTrade(CommandSourceStack source, String rawId) {
        ServerPlayer player = requirePlayer(source);
        Identifier tradeId = Identifier.tryParse(rawId);
        if (player == null || tradeId == null || !TradeManager.purchase(player, tradeId)) {
            return player == null ? 0 : failure(source, "Trade failed: invalid offer, insufficient coins, or inventory full.");
        }
        source.sendSuccess(() -> Component.literal("Trade completed: " + tradeId), false);
        return 1;
    }

    private static ServerPlayer requirePlayer(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command requires a player."));
        }
        return player;
    }

    private static int failure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
        return 0;
    }
}
