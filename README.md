# Lockout

A competitive Minecraft challenge plugin based on the Lockout gamemode concept: teams race to be the first to complete a curated list of tasks scattered across overworld, nether and end. Tasks are scriptable in **Lua**, so you can tailor every game to your players, your world, and your sense of humour.

---

## Table of contents

- [Requirements](#requirements)
- [Installation](#installation)
- [How a round works](#how-a-round-works)
- [Commands & permissions](#commands--permissions)
- [Configuration (`config.yml`)](#configuration-configyml)
- [Lua scripting](#lua-scripting)
  - [Where scripts live](#where-scripts-live)
  - [Board packages](#board-packages)
  - [The execution model](#the-execution-model)
  - [Bound globals](#bound-globals)
  - [Task builders](#task-builders)
  - [Composite tasks](#composite-tasks)
  - [Chained modifiers](#chained-modifiers)
  - [Rewards](#rewards)
  - [Predicates](#predicates)
  - [Game / helper functions](#game--helper-functions)
  - [The optional `init.lua`](#the-optional-initlua)
  - [Full example](#full-example)
- [Building from source](#building-from-source)

---

## Requirements

- **PaperMC 1.21 or newer.** The plugin uses Paper-specific APIs (region scheduler, advancement display, etc.); Spigot is not supported.
- **Java 21**.

## Installation

1. Drop `Lockout-<version>-all.jar` into your server's `plugins/` directory.
2. Start the server once so the plugin generates `plugins/Lockout/` with `config.yml` and `default/main.lua`.
3. Edit `config.yml` and the Lua task files to your liking, then `/lockout reload` (or restart).

## How a round works

A game has four phases:

| State       | What it means                                                     |
| ----------- | ----------------------------------------------------------------- |
| `PRE`       | Plugin booted, world not yet ready / players still joining.       |
| `READY`     | Boards and teams may be created. `/lockout start` is permitted.   |
| `STARTING`  | Countdown is on the bossbar; players are frozen if configured.    |
| `RUNNING`   | The game is live; events drive task completion and rewards.       |
| `TIEBREAKER`| Triggered when scores tie at game end (if `useTieBreaker=true`).  |
| `END`       | Final state; scoreboard frozen, rewards stop.                     |

Each task is worth one point unless the script changes the `value` argument. The team with the most points when the timer expires (or all tasks are claimed) wins.

## Commands & permissions

All commands are routed through `/lockout`:

| Command                  | Permission        | Description                                          |
| ------------------------ | ----------------- | ---------------------------------------------------- |
| `/lockout team <name>`   | `lockout.team`    | Create a custom team while in `READY`. The item in your main hand becomes the team's GUI icon. |
| `/lockout start`         | `lockout.start`   | Begin the countdown and start the game.              |
| `/lockout end`           | `lockout.end`     | Force the game to end.                               |
| `/lockout reload`        | `lockout.reload`  | Reload `config.yml` and re-run Lua chunks.           |
| `/lockout compass`       | -                 | Give yourself the player-tracking compass kit.       |
| `/lockout version`       | -                 | Show the running plugin version.                     |
| `/lockout eval <chunk>`  | OP (DEV rule)     | Evaluate a Lua chunk against the user environment. Only available when `dev` is enabled. |

Permission checks are only enforced when `commandsRequireOp=true` in `config.yml`; OPs always bypass.

## Configuration (`config.yml`)

| Key                          | Type    | Default  | Meaning                                                                 |
| ---------------------------- | ------- | -------- | ----------------------------------------------------------------------- |
| `autoLoad`                   | bool    | `true`   | Auto-load `autoLoadTask` on enable. If false, choose with the compass GUI. |
| `autoLoadTask`               | string  | `default`| Lua file (without `.lua`) inside `plugins/Lockout/`.                    |
| `world`                      | string  | `world`  | Name of the overworld used for the game.                                |
| `clearInventoryStart` / `clearInventoryEnd` | bool | `true` / `false` | Wipe player inventories on start / end. |
| `moveDuringCountdown`        | bool    | `false`  | Allow movement during the start countdown.                              |
| `countdownDuration`          | int     | `30`     | Countdown length in seconds.                                            |
| `defaultTeams`               | int     | `4`      | Number of auto-created wool-themed teams.                               |
| `maxTeams`                   | int     | `8`      | Cap for `/lockout team`.                                                |
| `teamSize`                   | int     | `4`      | Max players per team.                                                   |
| `enableReward`               | bool    | `true`   | Master switch for task rewards.                                         |
| `startAtSpawn`               | bool    | `false`  | Teleport all players to world spawn on start.                           |
| `forcePlayersOnTeam`         | bool    | `true`   | Auto-place stragglers onto a team at start.                             |
| `allowCompassTracking`       | bool    | `true`   | Enable the player-tracking compass.                                     |
| `commandsRequireOp`          | bool    | `true`   | Require permissions for privileged commands.                            |
| `useTimer` / `setTimer`      | bool / int | `true` / `2700` | Enable game timer (seconds).                                  |
| `useTieBreaker`              | bool    | `true`   | Run a tiebreaker round when scores are tied.                            |
| `startTime`                  | long    | `1000`   | World time (ticks) at game start.                                       |
| `useRespawnKit` / `respawnKitTime` / `respawnKitCooldown` | bool / int / int | `true` / `300` / `180` | Auto-issue a respawn kit after death. |
| `respawnInvulnerability` / `respawnInvulnerabilityTime` | bool / int | `true` / `140` | Brief invulnerability on respawn. |
| `revokeAdvancements`         | bool    | `true`   | Reset advancements at game start (needed for advancement tasks).        |
| `playerStateUpdateTicks`     | long    | `1`      | Tick interval for player-state task polling. Raise for very large lobbies. |
| `checkUpdate`                | bool    | `true`   | Notify OPs of available plugin updates.                                 |
| `dev`                        | bool    | unset    | Enable `/lockout eval` and verbose debug logging.                        |

---

# Lua scripting

Lockout uses [LuaJ](https://github.com/luaj/luaj) (Lua 5.2 dialect, JSE platform) to evaluate task scripts at runtime. Anything you put in your script — task definitions, rewards, predicates, helper code — runs **once at load time** to populate the in-memory task pool. The plugin then dispatches Bukkit events into the pool and your tasks are completed accordingly.

This section is the reference users care about most: every binding the plugin exposes, what arguments it takes, and what it returns.

## Where scripts live

```
plugins/
  Lockout/
    config.yml
    default/
      main.lua            ← bundled example board
    your-board/
      board.properties    ← optional, makes this a "board package"
      main.lua
    init.lua              ← optional, loaded at plugin startup if `dev` is enabled
```

- `config.yml`'s `autoLoadTask` picks which script under `plugins/Lockout/` is loaded.
- The directory containing the loaded script becomes the Lua "working directory" — `require("foo")` resolves `foo.lua` relative to it.
- Files are reloaded on `/lockout reload`.

## Board packages

A board is a self-contained directory with a `board.properties` file. Properties prefixed with `info.` are metadata; everything else is injected as a Lua variable **before** the entry script runs. This is how you ship configurable boards.

```ini
# plugins/Lockout/my-board/board.properties
info.name=My Board
info.author=stretch
info.version=1.0.0
info.pluginVersion=2.5.0
info.compatibility=1.21
info.init=plugins/Lockout/my-board/main.lua
info.description=A spicy challenge board

# Custom variables — must live inside a Lua table (dot notation).
config.timer=60
config.allowNether=true
config.startKit="diamond"
```

Inside `main.lua` you can then read `config.timer`, `config.allowNether`, etc. Boards are discovered by walking `plugins/Lockout/` for any directory containing `board.properties`.

## The execution model

When a script is loaded, the plugin:

1. Resets the Lua globals to a fresh JSE `standardGlobals()` (so `math`, `string`, `table`, `os` etc. are available).
2. Injects all of Lockout's bindings (the tables documented below) into the globals.
3. Runs the script top to bottom. Anything that calls `_addTask(...)` registers a task in the **main pool**; anything calling `_addTieBreaker(...)` goes into the tiebreaker pool.
4. Tasks subscribe to Bukkit event classes. When an event fires, every task subscribed to it is asked `doesAccomplish(event)`; the first matching task is awarded to the scoring player and locked out from everyone else.

## Bound globals

These are direct exposures of enums/classes — you reference them by their static fields just like in Java.

| Lua global       | Java type backing it                                  | Notes |
| ---------------- | ----------------------------------------------------- | ----- |
| `Bukkit`         | `org.bukkit.Bukkit`                                   | Static API; useful for `Bukkit:getVersion()`. |
| `Material`       | `org.bukkit.Material`                                 | E.g. `Material.DIAMOND_PICKAXE`. |
| `Biome`          | `org.bukkit.block.Biome`                              | E.g. `Biome.PLAINS`. |
| `BlockFace`      | `org.bukkit.block.BlockFace`                          | E.g. `BlockFace.DOWN`. |
| `Entity`         | `org.bukkit.entity.EntityType`                        | E.g. `Entity.ZOMBIE` (renamed for brevity). |
| `Enchantment`    | `org.bukkit.enchantments.Enchantment`                 | |
| `DamageCause`    | `EntityDamageEvent.DamageCause`                       | E.g. `DamageCause.FALL`. |
| `CauldronChange` | `CauldronLevelChangeEvent.ChangeReason`               | E.g. `CauldronChange.ARMOR_WASH`. |
| `DyeColor`       | `org.bukkit.DyeColor`                                 | |
| `Color`          | `org.bukkit.Color`                                    | |
| `Sound`          | `org.bukkit.Sound`                                    | |
| `ChatColor`      | `org.bukkit.ChatColor`                                | |
| `Attribute`      | `org.bukkit.attribute.Attribute`                      | |
| `Effect`         | `stretch.lockout.lua.LuaPotionEffect` (enum wrapper)  | Mirrors `PotionEffectType` with old-name aliases (e.g. `SLOW`, `FAST_DIGGING`, `JUMP`, `INCREASE_DAMAGE`). |
| `RewardType`     | `stretch.lockout.reward.RewardType`                   | `SOLO`, `TEAM`, `ENEMY`, `COMPOSITE`. |
| `GameRule`       | `stretch.lockout.game.LockoutGameRule`                | All boolean rules in `config.yml`. |
| `GameState`      | `stretch.lockout.game.state.GameState`                | `PRE`, `READY`, `STARTING`, `RUNNING`, `TIEBREAKER`, `PAUSED`, `END`. |
| `BiomeType`      | `stretch.lockout.util.types.BiomeTypes`               | Pre-defined biome sets — `JUNGLE`, `SNOW`, `SWAMP`, `SAVANNA`, `BADLANDS`, `DESERT`, `CHERRY`, `OAK`, `SPRUCE`, `BIRCH`, `OCEAN`, `FROZEN_OCEAN`. |
| `MaterialType`   | `stretch.lockout.util.types.MaterialTypes`            | Pre-defined material sets — `ORE`, `DEEPSLATE_ORE`, `DISC`, `TRAPDOOR`. |
| `Lockout`        | `LockoutWrapper` (live)                               | Methods: `getMaxScore()`, `setMaxScore(n)`, `getGameState()`, `getWorld()`. |
| `log`            | `stretch.lockout.util.MessageUtil`                    | E.g. `log:consoleLog("hi")`. |

Because LuaJ coerces Java types automatically, you can call any public method on any returned object just as you would in Bukkit/Paper: `player:getLocation()`, `player:getWorld():spawnEntity(loc, Entity.VEX)`, `block:getRelative(BlockFace.DOWN):getType()` etc.

## Task builders

Every task builder returns a `TaskComponent` you can chain modifiers on. The common signature is `name(arg, value, description, guiMaterial)`:

- `value` (int) — point value. Use `-1` for "negative" tasks (failing the condition is the goal, see [`takeDamage`](#task-builders)).
- `description` (string) — shown on the scoreboard and the task GUI.
- `guiMaterial` (Material) — icon used in the task selection inventory.

When a builder name has an `Any` suffix, the first argument is a `MaterialType` or `EntityType` set instead of a single value, and *any* member of the set satisfies the task.

| Function                                                      | Underlying Bukkit event                  | Description |
| ------------------------------------------------------------- | ---------------------------------------- | ----------- |
| `place(material, v, desc, gui)` / `placeAny(set,…)`            | `BlockPlaceEvent`                        | Place a block. |
| `destroy(material, v, desc, gui)` / `destroyAny(set,…)`        | `BlockBreakEvent`                        | Break a block. |
| `pickup(material, v, desc, gui)` / `pickupAny(set,…)`          | `EntityPickupItemEvent`                  | Pick up an item. |
| `drop(material, v, desc, gui)` / `dropAny(set,…)`              | `PlayerDropItemEvent`                    | Drop an item. |
| `obtain(material, v, desc, gui)` / `obtainAny(set,…)`          | OR of `InventoryClick`, `FurnaceExtract`, `CraftItem`, `Drop`, `PickupItem` | Acquire an item by any means. |
| `eat(material, v, desc, gui)` / `eatAny(set,…)`                | `PlayerItemConsumeEvent`                 | Eat / drink. |
| `smelt(material, v, desc, gui)` / `smeltAny(set,…)`            | `FurnaceExtractEvent`                    | Pull a smelted item out of a furnace. |
| `kill(entity, v, desc, gui)` / `killAny(set,…)`                | `EntityDeathEvent`                       | Kill a mob (you must have done the killing blow). |
| `hit(entity, v, desc, gui)` / `hitAny(set,…)`                  | `EntityDamageByEntityEvent`              | Hit a mob at least once. |
| `breed(entity, v, desc, gui)` / `breedAny(set,…)`              | `EntityBreedEvent`                       | Breed a pair. |
| `tame(entity, v, desc, gui)` / `tameAny(set,…)`                | `EntityTameEvent`                        | Tame a mob. |
| `shear(entity, v, desc, gui)`                                  | `PlayerShearEntityEvent`                 | Shear a mob. |
| `bucket(entity, v, desc, gui)`                                 | `PlayerBucketFishEvent` or `PlayerBucketEntityEvent` (axolotl) | Bucket a fish / axolotl. |
| `interactEntity(entity, v, desc, gui)` / `interactEntityAny(set,…)` | `PlayerInteractEntityEvent`         | Right-click a mob. |
| `interactBlock(material, v, desc, gui)` / `interactBlockAny(set,…)` | `PlayerInteractEvent`               | Right-click a block. |
| `cauldron(reason, v, desc, gui)`                               | `CauldronLevelChangeEvent`               | `reason` is a `CauldronChange.*` value. |
| `damageByEntity(entity, cause, v, desc, gui)`                  | `EntityDamageByEntityEvent`              | Take damage from a specific entity type and cause. |
| `damageByBlock(material, cause, v, desc, gui)`                 | `EntityDamageByBlockEvent`               | Take damage from a block of a specific material and cause. |
| `takeDamage(cause, v, desc, gui)`                              | `EntityDamageEvent`                      | Take damage from any source with the given cause. Use with `-1` for "don't take fall damage" style tasks. |
| `standOn(material, v, desc, gui)`                              | `PlayerStateChangeEvent`                 | The block underneath the player matches. |
| `standOnAny(set, v, desc, gui)`                                | `PlayerMoveEvent`                        | The block underneath the player is in the given set. |
| `playerState(v, desc, gui, predicate)`                         | `PlayerStateChangeEvent`                 | Predicate is called every `playerStateUpdateTicks` ticks. |
| `quest(eventClassName, v, desc, gui)`                          | the given event                          | Escape hatch: bind to any Bukkit event by its fully-qualified class name, e.g. `"org.bukkit.event.player.PlayerLevelChangeEvent"`. |
| `advancement(title, v, desc, gui)`                             | `PlayerAdvancementDoneEvent`             | Compare against the advancement's display name (case-insensitive). Requires `revokeAdvancements=true` for fairness. |
| `structure(v, desc, gui, blockPredicate)`                      | `BlockPlaceEvent`                        | Fires when a placed block satisfies `blockPredicate(block)`. |
| `pvp(v, desc, gui)`                                            | PvP kill                                 | Kill any enemy player. Mostly used for tiebreakers. |
| `getEffect(potionEffectType, v, desc, gui)`                    | `EntityPotionEffectEvent`                | Gain a specific potion effect. |

## Composite tasks

Combine tasks into larger logical units. All composite builders return a `TaskComponent` that can itself be added with `_addTask` or further nested.

| Function                                              | Behaviour |
| ----------------------------------------------------- | --------- |
| `_anyOf(gui, value, desc, taskA, taskB, …)`           | Completes when any child completes. |
| `_allOf(gui, value, desc, taskA, taskB, …)`           | Completes when all children complete (any order). |
| `_sequential(gui, value, desc, taskA, taskB, …)`      | Completes when all children complete in the given order. |
| `_doTimes(n, task)`                                   | Completes when `task` has been completed `n` times. |
| `_oneOf(taskA, taskB, …)`                             | Pure helper: picks one of the supplied tasks at random *at script load time* and returns it. |

> All five also have unprefixed aliases when wired through `init.lua` (see [The optional `init.lua`](#the-optional-initlua)). The bundled `default/main.lua` calls them without the underscore.

## Chained modifiers

Returned tasks are Java objects, so you call modifiers with Lua's `:` colon syntax:

```lua
local task = kill(Entity.VILLAGER, 1, "Kill a villager with a bone", Material.BONE)
  :addPlayerCondition(function(player) return inMainHand(player, Material.BONE) end)
  :addEntityCondition(function(mob)    return mob:getType() == Entity.VILLAGER end)
  :setReward(rewards(speed_potion, iron_chestplate))
  :setDescription("Custom description")
  :setValue(2)
```

| Method                       | Available on                  | Effect |
| ---------------------------- | ----------------------------- | ------ |
| `:addPlayerCondition(fn)`    | any task                      | `fn(player)` must return `true` for the event to count. Stackable. |
| `:addEntityCondition(fn)`    | entity tasks (`kill`, `hit`, `breed`, `tame`, `shear`, `bucket`, `interactEntity`, `damageByEntity`) | `fn(mob)` must return `true`. Stackable. |
| `:setReward(rewardComponent)`| any task                      | Attach a reward. See below. |
| `:setDescription(str)`       | any task                      | Override description. |
| `:setValue(int)`             | any task                      | Override point value. |
| `:setGuiItemStack(item)` / `:setDisplay(material)` | any task | Override the GUI icon. |

For `playerState` and `structure` the predicate is the **constructor argument**, not a chained method. Predicates also have access to the full Bukkit `Player` / `Block` API since they receive live Java objects.

## Rewards

Rewards are produced by their own builders and applied automatically when a task completes (assuming `enableReward=true`).

| Function                                                                          | Description |
| --------------------------------------------------------------------------------- | ----------- |
| `item(material, amount, rewardType, description [, enchants] [, itemMetaFn])`     | Build an item-stack reward. `enchants` is a Lua table whose keys are enchantment names (snake_case, e.g. `efficiency=4`, `feather_falling=3`). `itemMetaFn(itemStack)` is an optional callback to mutate the stack. Old enchantment names (`arrow_damage`, `dig_speed`, `protection_environmental`, etc.) are auto-translated. |
| `potion(effect, amplifier, rewardType, description [, durationTicks])`            | Apply a potion effect. Amplifier is 1-based (`1` = level I). Duration defaults to `LockoutSettings.DEFAULT_REWARD_POTION_TICKS` (= 144000 = 2 hours). |
| `action(rewardType, description, function(player) … end)`                         | Arbitrary Lua callback executed with the target player. |
| `rewardChance(description, { [rewardA]=weight, [rewardB]=weight, … })`            | Weighted random pick among the given rewards. Keys must be `RewardComponent`s. |
| `rewardTask(rewardType, description, task)`                                       | Wraps `task` as an *invisible* gating task: the reward "activates" it for the receiving player. Pair with `:getDelegate():unsubscribeAfterTime(ticks)` (see example) to time-box it. |
| `rewards(rewardA, rewardB, …)`                                                    | Compose multiple rewards into one. |

`RewardType` controls who is targeted:

- `SOLO` — only the player who completed the task.
- `TEAM` — everyone on the scoring team.
- `ENEMY` — every player **not** on the scoring team.
- `COMPOSITE` — used internally by `rewards(...)`; you typically don't set this directly.

## Predicates

Predicates are plain Lua functions, but Lockout ships ready-made helpers that cover the common cases. They all return a boolean and can be combined with normal Lua `and` / `or` / `not`.

| Function                              | True when… |
| ------------------------------------- | ---------- |
| `inMainHand(entity, material)`        | Entity's main-hand item is `material`. |
| `inOffHand(entity, material)`         | Entity's off-hand item is `material`. |
| `inBiome(entity, biome)`              | Entity is standing in `biome`. |
| `inBiomeType(entity, biomeSet)`       | Entity is in any biome in the set (use a `BiomeType.*`). |
| `onBlock(entity, material)`           | The block directly below the entity is `material`. |
| `onSurface(entity)`                   | The block at the entity's feet has skylight level 15. |
| `aboveY(entity, y)`                   | Entity's Y ≥ `y`. |
| `hasMaxHealth(entity)`                | Entity's current health equals its max-health attribute. |
| `wearingArmor(entity, material)`      | Any equipped armor piece is `material`. |
| `hasPotionEffect(entity, effect)`     | Entity has the given `Effect.*` active. |
| `onFire(entity)`                      | `getFireTicks() > 0`. |
| `inWater(entity)`                     | `entity:isInWater()`. |
| `isDay(entity)`                       | World time < 13000 (Minecraft day). |
| `isEntity(entity, entityType)`        | `entity:getType() == entityType`. |

For block predicates passed to `structure(...)`, you receive a Bukkit `Block` and must use its API directly (e.g. `block:getRelative(BlockFace.DOWN):getType()`).

## Game / helper functions

These are the underscore-prefixed helpers exposed by `LuaHelperBindings`. They drive the game state from inside your script.

| Function                              | Purpose |
| ------------------------------------- | ------- |
| `_addTask(taskComponent)`             | Register `taskComponent` in the active pool (main tasks during `READY`, tiebreaker tasks during `TIEBREAKER`). |
| `_addTieBreaker(taskComponent)`       | Force-register a task in the tiebreaker pool regardless of state. |
| `_taskCount()`                        | Number of visible tasks currently registered. |
| `_setMaxScore(n)`                     | Set the point target. |
| `_setTimer(minutes)`                  | Override the timer (in **minutes**) if `useTimer=true`. |
| `_trackedPlayer(player)`              | The player currently tracked by `player` via compass; defaults to `player`. |
| `_containsBiomeType(biomeSet, biomeType)` | `true` if `biomeSet` (from `_getBiomes`) intersects with `biomeType` (from `BiomeType.*`). |
| `_getBiomes(location, distance)`      | Scan a snowflake-shaped area of `distance` 15-block steps in 8 directions around `location` and return a `Set<Biome>` of biomes encountered. Runs on Paper's region scheduler. |
| `_createItem(material, amount)`       | `new ItemStack(material, amount)`. |
| `_copyItem(itemStack)`                | Shallow-clone an ItemStack. |
| `_createLoc(world, x, y, z)`          | `new Location(world, x, y, z)`. |
| `_anyOf` / `_allOf` / `_sequential` / `_doTimes` / `_oneOf` | See [Composite tasks](#composite-tasks). |

## The optional `init.lua`

`plugins/Lockout/init.lua` is loaded automatically at plugin startup **if `dev=true`**, and on `/lockout reload`. It's the right place to set up aliases, project-wide helpers, or anything you want available before your board scripts run.

The bundled `default/main.lua` calls the helpers *without* the underscore prefix (`addTask`, `anyOf`, `setTimer`, …). To make those scripts work, drop an `init.lua` like this:

```lua
-- plugins/Lockout/init.lua
-- Friendly aliases for the underscore-prefixed helper bindings.
addTask           = _addTask
addTieBreaker     = _addTieBreaker
taskCount         = _taskCount
setMaxScore       = _setMaxScore
setTimer          = _setTimer
trackedPlayer     = _trackedPlayer
containsBiomeType = _containsBiomeType
getBiomes         = _getBiomes
createItem        = _createItem
copyItem          = _copyItem
createLoc         = _createLoc
anyOf             = _anyOf
allOf             = _allOf
sequential        = _sequential
doTimes           = _doTimes
oneOf             = _oneOf

-- Older versions called these `addPlayerPredicate` / `addEntityPredicate`.
-- LuaJ resolves methods by reflection, so you only need shims if your scripts
-- still call the older names:
--
-- local function alias(task)
--   task.addPlayerPredicate = task.addPlayerCondition
--   task.addEntityPredicate = task.addEntityCondition
--   return task
-- end
```

## Full example

A small, self-contained board that drops three tasks and a tiebreaker:

```lua
-- plugins/Lockout/sample/main.lua

_setTimer(45)                              -- 45-minute round
local spawn = Lockout:getWorld():getSpawnLocation()
local biomes = _getBiomes(spawn, 67)        -- discover nearby biomes

-- Rewards
local gapple   = item(Material.ENCHANTED_GOLDEN_APPLE, 1, RewardType.SOLO, "Gapple")
local pickaxe  = item(Material.DIAMOND_PICKAXE, 1, RewardType.TEAM, "Diamond pick", {efficiency = 4})
local blind    = potion(Effect.BLINDNESS, 1, RewardType.ENEMY, "Blind enemies", 1200)
local smite_me = action(RewardType.SOLO, "Boom", function(player)
                          local loc = player:getLocation()
                          player:getWorld():strikeLightning(loc)
                       end)

-- Tasks
_addTask(
  kill(Entity.WITCH, 1, "Kill a witch", Material.CAULDRON)
    :setReward(blind)
)

_addTask(
  obtain(Material.EMERALD, 1, "Find an emerald", Material.EMERALD)
    :setReward(pickaxe)
)

_addTask(
  _doTimes(5,
    place(Material.TORCH, 1, "Place 5 torches at night", Material.TORCH)
      :addPlayerCondition(function(p) return not isDay(p) end)
  ):setReward(gapple)
)

-- Conditional tasks based on world biomes
if _containsBiomeType(biomes, BiomeType.OCEAN) then
  _addTask(
    bucket(Entity.AXOLOTL, 1, "Bucket an axolotl", Material.AXOLOTL_BUCKET)
      :setReward(rewards(gapple, smite_me))
  )
end

-- Tiebreaker
_addTieBreaker(pvp(1, "Kill an enemy player", Material.DIAMOND_SWORD))
```

For a far more elaborate example, read `src/main/resources/default/main.lua` — it builds a full ~27-task board with biome-aware tasks, weighted reward chances, negative ("don't do X") tasks, and a time-boxed wrapper reward built on `rewardTask` + `unsubscribeAfterTime`.

---

## Building from source

```sh
./gradlew shadowJar
```

The shaded jar lands in `build/libs/Lockout-<version>-all.jar`. Drop it into your test server's `plugins/` directory.

Dependencies of note:

- `io.papermc.paper:paper-api` 1.21.4 (compile-only)
- `org.luaj:luaj-jse` 3.0.1
- `com.google.code.gson:gson` 2.8.9

Java 21 is the source/target level.
