# VIBECODING!!

# Re:Zero: Starting Life in Another World

Server-authoritative Fabric RPG foundation for Minecraft Java Edition `1.21.11`.

## Current Vertical Slice

- Persistent player RPG data: level, experience, mana, stamina, primary stats.
- Server-side mana/stamina regeneration and resource validation.
- Typed Fabric payloads. Clients send intent only; the server validates targets, range, cooldowns, resources, and ability IDs.
- `rezero:shadow_bind`: a Yin magic ability with a 20 mana cost and server-side Slowness effect.
- Return by Death: automatic checkpoint creation, fatal-damage interception, inventory/player/story-state restoration, teleportation, and a duplicate-restore guard.
- Datapack-driven quest, dialogue, and NPC-profile loading. Invalid entries log an error and are skipped without crashing the server.
- Persistent player quest status, story flags, and NPC relationships (`-100..100`).
- Story NPC vertical slice: spawn a profile-bound NPC, right-click it, then choose dialogue through commands.
- Data-driven quest objectives: `talk` and `kill`, recorded only from server events and completed server-side.
- Synchronized Character, Magic, and Quest Journal screens, opened by `R`, `M`, and `J` respectively.
- Dialogue screen with server-issued text/choices and server-validated response packets.
- Persistent learned spells with server-side unlock validation and datapack spell balance definitions.
- Persistent authority/divine-protection ownership, an explicit tracked-story-state store, and an Elsa boss runtime prototype with boss bar/phases.
- Initial economy registry with Lugunica copper/silver/gold coins and a Magic Stone material.
- Custom `rezero:wolgarm` entity with server-controlled spawn cap/interval, night/story gating, and a vanilla wolf renderer adapter.
- Persistent world-event state plus White Whale/Witch Cult datapack profiles.
- Boss mechanics: Elsa berserk dash/bleed pressure; White Whale fog, phantom summons, and curse phases; automated event availability/completion state.
- Two persistent accessory slots for Magic Stones with inventory-safe server-side equip/unequip commands.
- NPC availability schedules (`always`, `day`, `night`) evaluated only server-side.
- Data-driven Authority/Divine Protection registries, server activation/passive behavior, synchronization, and authority screen (`P`).
- Custom Great Rabbit entity/renderer adapter and story landmark registrations for Mansion, Sanctuary, and Royal Capital.
- Reach-location objectives, bounded dev landmark shell builders, data-driven relationship-priced commerce, Relationship UI (`O`), and dialogue branch conditions.
- Return by Death transition screen, server-side collect/survive objectives, expanded dialogue actions, and tracked-state mutation utilities.
- RPG level progression from combat/quest rewards, server-authoritative stamina Dodge, faction reputation persistence, Witch Cultist controller, and schema-v2 save migration hooks.
- Faction reputation UI (`I`), server-authoritative Mana Potion, capped Great Rabbit population controller, and world-event completion rewards.
- Petelgeuse boss profile/mechanics, Witch Cult summons, story boss actions, and boss/event reference validation.
- Boss rewards/objectives, retained Return by Death memories, tracked block restoration, and story-granted magic/authority/protection actions.
- Persistent profile-bound NPC placements, quest chains/faction rewards, and world-event cleanup.
- Return by Death NPC restoration, Wind/Earth spell implementations, and server-synchronized Ability cooldown UI (`K`).
- English and Russian localization for currently exposed keys.

## Requirements

- Minecraft Java Edition `1.21.11`.
- Fabric Loader `0.19.5` or newer.
- Fabric API `0.141.6+1.21.11`.
- JDK `21` to compile mod source. Current Fabric Loom development tooling may require a newer JDK to run Gradle itself.

## Build

```sh
./gradlew build
```

The remapped mod JAR is written to:

```text
build/libs/rezero-0.1.0.jar
```

The project includes Gradle Wrapper files. Add `rezero-0.1.0.jar` and the matching Fabric API JAR to the instance/server `mods` directory.

## Configuration

The mod creates `config/rezero.json5` on first launch. Available settings:

- `enableMagic`
- `enableAuthorities`
- `enableReturnByDeath`
- `enableStory`
- `difficulty`: `easy`, `normal`, `hard`, or `nightmare`
- `bossMultiplier`
- `mobSpawnRate`
- `manaRegeneration`
- `staminaRegeneration`
- `friendlyFire`

An unreadable config falls back to safe defaults and logs the error.

## Commands

All commands are available as `/rezero ...`.

| Command | Access | Purpose |
| --- | --- | --- |
| `stats` | player | Show current mana/stamina and level. |
| `checkpoint set` | gamemaster | Create a Return by Death checkpoint. |
| `quest start <id>` | player | Start a loaded quest. |
| `quest complete <id>` | gamemaster | Complete an active quest and grant configured XP. |
| `dialogue start <id>` | player | Print the first dialogue node and options. |
| `dialogue choose <dialogue> <node> <index>` | player | Submit a displayed dialogue choice. |
| `npc spawn <profile> [position]` | gamemaster | Spawn a profile-bound story NPC. |
| `story flags` | player | Show the current player story flags. |
| `story relationship <npc>` | player | Show relationship value with an NPC profile. |
| `magic learn <spell>` | gamemaster | Grant a loaded spell to the executing player. |
| `magic list` | player | List server-authorized learned spells. |
| `authority grant <id>` | gamemaster | Grant a Re:Zero authority ID. |
| `divine grant <id>` | gamemaster | Grant a Re:Zero divine protection ID. |
| `world set <key> <value>` | gamemaster | Set explicit Return by Death tracked world state. |
| `boss spawn <id> [position]` | gamemaster | Spawn a configured prototype boss. |
| `event start <id>` | gamemaster | Start an available/inactive configured world event. |
| `equipment equip <first\|second>` | player | Equip held Magic Stone as an accessory. |
| `equipment unequip <first\|second>` | player | Return an accessory to inventory. |
| `world sanctuary locate` | gamemaster | Register the Sanctuary story landmark. |
| `world capital locate` | gamemaster | Register the Royal Capital story landmark. |
| `world build <mansion\|sanctuary\|capital>` | gamemaster | Build a bounded development landmark shell. |
| `trade buy <id>` | player | Purchase a server-defined trade offer. |
| `quest reset <id>` | gamemaster | Reset one quest status/progress entry. |
| `spawn cultist` | gamemaster | Spawn a marked Witch Cultist adapter. |
| `rabbit_swarm` | gamemaster | Start a capped Great Rabbit swarm near yourself. |
| `boss spawn rezero:petelgeuse` | gamemaster | Spawn the Petelgeuse combat prototype. |
| `npc restore <profile>` | gamemaster | Restore a recorded NPC placement in its original dimension. |
| `event fail <id>` | gamemaster | Mark an event failed and remove its tagged entities. |
| `debug player` | gamemaster | Show Return by Death state. |

Examples:

```mcfunction
/rezero npc spawn rezero:emilia
/rezero dialogue start rezero:emilia_intro
/rezero dialogue choose rezero:emilia_intro start 0
/rezero dialogue choose rezero:emilia_intro help 0
```

The spawned entity displays its profile ID for debugging. Right-clicking it starts its configured dialogue and adds one relationship point. It is an MVP adapter built on a named vanilla villager, not a final custom character model.

NPC profile spawns now store profile ID, dimension, position, and rotation in world persistent data. Spawned entities carry a stable profile tag, so dialogue is not dependent on mutable display names. `/rezero npc restore <profile>` recreates a recorded profile-bound placement when it is missing; it does not silently mass-spawn all profiles.

Return by Death checkpoints include profile-bound NPC spawn records. On restore, only tagged story NPC adapters are removed and recreated from checkpoint records; generic villagers and unrelated entities are untouched.

Dialogue opening and choice effects are server-authoritative. The client renders only an `OpenDialoguePayload`; its response contains the selected index, which the server validates against the active player dialogue session before applying dialogue actions.

Implemented dialogue actions are `start_quest`, `complete_quest`, `set_flag`, `change_relationship`, `give_item`, `teleport_landmark`, `start_boss`, `set_world_event`, `learn_spell`, `grant_authority`, `grant_divine_protection`, `retain_memory`, and `create_checkpoint`. Unsupported action types log and are ignored rather than executing arbitrary data.

## Quest Objectives

Quest objectives are declared in a quest definition and are not accepted from client packets. The current runtime supports:

- `talk`: advances from a successful interaction with the configured story NPC profile.
- `kill`: advances from Fabric's server-side combat death event when the player kills the configured entity type.
- `use_ability`: advances after a server-validated ability completes.
- `reach_location`: advances from a server position check against a persistent story landmark.
- `collect`: advances from server inventory inspection; client inventory packets are not trusted.
- `survive`: advances when the server reaches a new day with the player alive.

```json
{
  "title": "Arrival",
  "description": "Find Emilia.",
  "objectives": [
    { "type": "talk", "target": "rezero:emilia", "count": 1 }
  ],
  "experience_reward": 100
}
```

## Data Packs

Definitions are loaded on server start and after `/reload` from all active data packs. Entries use their file namespace and filename as IDs.

| Content | Directory | Example ID |
| --- | --- | --- |
| Quests | `data/<namespace>/quests/*.json` | `rezero:arrival` |
| Dialogues | `data/<namespace>/dialogues/*.json` | `rezero:emilia_intro` |
| NPC profiles | `data/<namespace>/entities/*.json` | `rezero:emilia` |
| Spells | `data/<namespace>/spells/*.json` | `rezero:shadow_bind` |

The included quest and dialogue definitions demonstrate the runtime schema:

```json
{
  "title": "Arrival",
  "description": "Find someone in the unfamiliar city.",
  "experience_reward": 100
}
```

## Quest Chains

Quest definitions can declare `prerequisites`. The server refuses a quest start until all referenced quests are `COMPLETED`. Completed quests set `quest.<id>.completed` story flags and can grant bounded faction reputation rewards. The current content connects Arrival → Stolen Insignia → Loot House → Mansion Arc.

```json
{
  "speaker": "Emilia",
  "start": "start",
  "nodes": {
    "start": {
      "text": "Are you lost?",
      "choices": [
        { "text": "Yes.", "next": "help" }
      ]
    }
  }
}
```

Implemented dialogue actions are intentionally allowlisted: `start_quest`, `complete_quest`, and `set_flag`.

## Return by Death Scope

The current checkpoint snapshot restores player dimension/position/rotation, health, inventory, RPG data, story data, and the explicit `TrackedStoryState` map for its checkpoint dimension. It does not restore arbitrary blocks, inventories, entities, or chunks. Future story blocks/NPCs/chests must register only their required values in this map; a full-world snapshot is intentionally not used.

Flags prefixed with `memory.` survive a checkpoint restoration. Learned spells are retained by merging current-timeline magic knowledge into the checkpoint magic state. `/rezero world track_block` registers the block below the executor as explicit story-block state, making it eligible for bounded block-state restoration; it never snapshots arbitrary chunks.

On a server-issued restore payload, the client displays a short non-dismissible Return by Death transition screen while the server performs the actual restore. The visual transition never owns death, teleport, inventories, health, or story state.

## Boss Prototype

Boss profiles load from `data/<namespace>/bosses/*.json`. The Elsa profile currently uses a `minecraft:vindicator` entity adapter, configured max health, a server boss bar, proximity tracking, and named phase transitions. It is a gameplay/runtime prototype, not the final Elsa model, animation set, or scripted AI.

## Economy and Effects

Stable item IDs are present for `rezero:lugunica_copper_coin`, `rezero:lugunica_silver_coin`, `rezero:lugunica_gold_coin`, and `rezero:magic_stone`. They intentionally use vanilla placeholder visuals pending licensed art.

`rezero:bleed` is a server-ticked harmful effect. The Elsa berserk prototype applies it to nearby boss-bar players once the phase begins.

Magic damage passes through a shared server-side `DamageContext`/`DamageManager` pipeline. It applies caster Magic Power and target Magic Resistance; true damage bypasses defenses.

## World Scope

`/rezero world mansion locate` registers the Roswaal Mansion landmark in explicit tracked story state at the command location. This is a safe progression anchor for the MVP story route; it is not final Jigsaw structure generation. The eventual mansion/sanctuary/royal-capital content requires authored templates, biome placement rules, and licensed assets.

## Witchbeasts and Events

`rezero:wolgarm` is an executable custom entity. `/rezero spawn wolgarm` creates one for test/dev work. After the `visited_mansion` flag exists, the logical server evaluates each player every 30 seconds during Overworld night and spawns at most four nearby Wolgarms per player. This avoids per-tick global scans and prevents entity floods.

`WorldEventManager` persists named event states: `INACTIVE`, `AVAILABLE`, `ACTIVE`, `COMPLETED`, and `FAILED`. Administrators can set a state with `/rezero event set <id> <status>`. The White Whale data profile is loaded and can be started via `/rezero boss spawn rezero:white_whale`; final weather/fog/summon choreography remains authored boss content.

During an Overworld thunderstorm at night, the server checks every 30 seconds for a player meeting the configured story-flag gate. Eligible `INACTIVE` events become `AVAILABLE`. Starting a world event spawns its configured boss; boss death marks its attached event `COMPLETED`.

`/rezero event fail <id>` changes an active event to `FAILED` and cleans up only entities explicitly tagged for that event. It does not affect unrelated entities.

## Equipment

`EquipmentData` persists two accessory slots independently from vanilla armor. The first supported accessory is `rezero:magic_stone`; server commands transfer exactly one held item, reject non-accessories, return replaced accessories only when inventory has room, and prevent inventory loss/duplication. Stat modifiers and GUI equipment slots remain pending content work.

## Authorities

Authority and Divine Protection IDs are data-driven and persistent. The first active authority is `rezero:invisible_hand`; after an administrator grants it with `/rezero authority grant rezero:invisible_hand`, a future client binding can send target intent through `UseAuthorityPayload`. The server verifies ownership, mana, cooldown, living target, and range before knockback/Curse. `rezero:divine_protection_fire` passively clears fire ticks on its owner. `P` opens a read-only synchronized Authority screen.

## Locations

Mansion, Sanctuary, and Royal Capital currently register durable story landmarks in `TrackedStoryState`. This supplies progression/Return by Death state without pretending to ship absent Jigsaw template assets. The concrete Jigsaw structures require authored templates and world-generation balancing.

`/rezero world build <landmark>` creates a bounded dev shell from vanilla blocks on clear ground. It is intended for testing landmarks and quest reach behavior, not as a replacement for an authored structure. Landmark coordinates are persisted, and `reach_location` objectives evaluate at a one-second server interval inside a 24-block radius.

## Economy and Relationships

Trades load from `data/<namespace>/trades/*.json`. `/rezero trade buy <id>` resolves price, relationship discount, currency removal, inventory capacity, and reward creation on the server. Example `rezero:emilia_magic_stone` accepts Lugunica Copper Coins and discounts each positive ten relationship points. `O` opens the synchronized relationship screen.

Dialogue choices now support server-side `conditions`: `story_flag`, `quest_active`, `quest_completed`, `player_level`, and `relationship`. Hidden choices are never sent to the client, and choice indexes are still verified against the active server dialogue session.

## Progression and Combat

Re:Zero RPG levels are independent of vanilla XP. Server quest completion and server-confirmed living-entity kills grant persistent RPG experience. Level thresholds are `100 + (level - 1) * 50`; each level increases maximum mana/stamina and primary combat stats, up to level 100.

`Left Alt` sends Dodge intent only. The server requires 20 stamina, applies its own 20-tick cooldown, then applies the movement impulse. Clients never provide final velocity, stamina, or cooldown state.

Wind `rezero:fura` uses server knockback + Weakness. Earth `rezero:dona` applies server damage + heavy Slowness. `K` opens the Ability screen, which displays `READY` or cooldown time exclusively from server-sent ready-at ticks. It does not calculate cooldowns client-side.

Faction reputation persists alongside NPC relationships and changes on successful NPC interactions. Witch Cultists use an explicitly marked Vindicator adapter with nearby-player targeting and server-applied Curse. This remains an adapter pending a custom cultist model/entity.

`I` opens synchronized faction reputation. `rezero:mana_potion` restores 40 mana only on the server and cannot be consumed at full mana. It is crafted from a Magic Stone and glass bottle.

Great Rabbit uses a population controller rather than an unbounded entity flood: a dev swarm starts with four members, checks every five seconds, and caps each nearby swarm at twelve members. World-event boss completion awards nearby players two Lugunica Gold Coins and sets the player flag `<event>_defeated`.

## Petelgeuse Prototype

`rezero:petelgeuse` uses an Evoker adapter with three server-controlled phases: Invisible Hand knockback/Curse, Witch Cultist summons, then bounded nearby reposition with Blindness. Otto's `rezero:witch_cult_warning` dialogue starts the Witch Cult Assault quest and can spawn the boss through an allowlisted `start_boss` action. This is a combat/runtime prototype, not final Petelgeuse visuals, animations, voice, or canon encounter scripting.

Boss definitions configure RPG XP and Lugunica Gold rewards. Server boss defeat grants rewards to boss-bar participants and records `boss` quest objectives using the definition ID rather than relying on the underlying adapter entity type.

## Save Compatibility

`DataVersion.CURRENT` and `MigrationHandler` establish the persistent-data migration entry point. The current schema is v2: faction reputation was added with a codec default for v1 worlds. Future serialized component/world data changes must increment the schema and add an explicit migration rather than silently invalidating a world.

## Tests

```sh
./gradlew test
./gradlew build
```

Current unit coverage verifies resource clamping/regeneration, level progression/thresholds, quest progress initialization/capping/reset/chains, relationship/faction bounds, NPC spawn records/snapshots, boss definition bounds, world event outcomes/persistence/definitions, damage calculations, RBD story/magic/NPC retention, tracked story state/mutations, accessory slot definitions, authority/protection data, and save migration hooks. Dedicated-server and in-world integration testing remain required before release.

## CI

`.github/workflows/build.yml` runs `./gradlew build` for pushes to `main`/`develop` and pull requests using Temurin JDK 25, matching the current Fabric Loom toolchain requirement.

## Project State

Implemented: server-authoritative player/magic/story/quest data; typed networking; RBD player plus explicit story-state restore; quest talk/kill objectives; dialogue UI; character/magic/quest UI; spell unlocks; shared damage pipeline; item/effect registries; authority/protection ownership; boss runtime prototype; save schema marker; unit tests; CI.

Not release-complete: custom player-character/NPC entity classes and models for Subaru, Emilia, Rem, Ram, Roswaal, Beatrice, Elsa, Petelgeuse, Reinhard and others; custom Great Rabbit/White Whale models and advanced AI; authored Mansion/Sanctuary/Royal Capital Jigsaw templates; all requested story arcs/dialogue branches/NPC schedules; boss scripted AI choreography; particles/sounds/music; accessory stat modifiers and GUI equipment slots; full merchant/trading UI; quest types beyond talk/kill/use-ability/reach-location/collect/survive/boss; dedicated-server in-world integration suite; licensed or original art/audio assets. Wolgarm and Great Rabbit are current custom entities using vanilla renderer adapters; other named combat encounters currently use data-driven vanilla entity adapters. Story locations are persistent progression landmarks and dev shells, not generated structures. These deliverables cannot be honestly represented as complete without the authored source assets, canon review, and gameplay testing.

## License

MIT. Re:Zero names and setting references remain the property of their respective rights holders. Do not ship copyrighted visual/audio assets without the required rights.
