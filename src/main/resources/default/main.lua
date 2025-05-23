setTimer(45)

function shuffle(tbl)
for i = #tbl, 2, -1 do
local j = math.random(i)
tbl[i], tbl[j] = tbl[j], tbl[i]
end
return tbl
end

function take(n, tasks)
for i=1, n do
addTask(tasks[i])
end
return tasks
end

function glow_in_hand(player)
return inMainHand(player, Material.GLOW_INK_SAC)
end

function spawn_vex(player)
local loc = player:getLocation()
for i=1, 5 do
player:getWorld():spawnEntity(loc, Entity.VEX)
end
end

function smite(player)
local loc = player:getLocation()
local world = player:getWorld()
world:createExplosion(loc, 1)
world:strikeLightning(loc)
end

function tp_tracked(player)
local target = trackedPlayer(player)
local player_loc = player:getLocation()

if target then
player:teleport(target)
target:teleport(player_loc)
target:playSound(target, Sound.BLOCK_BELL_RESONATE, 1, 1)
player:playSound(player, Sound.BLOCK_BELL_RESONATE, 1, 1)
end
end

function swap_inv(player, swap_item_mat, amount)
local inv = player:getInventory()
local index = inv:getHeldItemSlot()
local item = createItem(swap_item_mat, amount)

local target_player = trackedPlayer(player)
local target_inv = target_player:getInventory()
local target_item = target_inv:getItem(index)
if target_item ~= nil and target_item:getType() ~= Material.HONEY_BOTTLE then
player:getWorld():dropItemNaturally(player:getLocation(), target_item)
end
target_inv:setItem(index, item)
target_player:playSound(target_player, Sound.BLOCK_BELL_RESONATE, 1, 1)
end

function prepare_tasks(nested_tasks)
local result = {}
for _, i in ipairs(nested_tasks) do
for _, j in ipairs(i) do
table.insert(result, j)
end
end
return shuffle(result)
end
----
local start = Lockout:getWorld():getSpawnLocation()
local biomes = getBiomes(start, 67)


-- Rewards
local swap_glass_bottle = action(RewardType.SOLO, "Swap item with tracked player",
                                                  function (player)
                                                  swap_inv(player, Material.GLASS_BOTTLE, 1) end)
local swap_stick = action(RewardType.SOLO, "Swap item with tracked player",
                                           function (player)
                                           swap_inv(player, Material.STICK, 1) end)

local speed_II_temp = potion(Effect.SPEED, 2, RewardType.TEAM, "Speed I temporary", 2400)
local slowness_II_enemies_temp = potion(Effect.SLOWNESS, 2, RewardType.ENEMY, "Slowness for enemies", 2400)
local iron_pickaxe = item(Material.IRON_PICKAXE, 1, RewardType.TEAM, "Iron pickaxe", {efficiency=4})
local rabbit = item(Material.COOKED_RABBIT, 15, RewardType.TEAM, "15 cooked rabbit")
local netherite_shovel = item(Material.NETHERITE_SHOVEL, 1, RewardType.SOLO, "Netherite shovel", {efficiency=4})
local iron_chestplate = item(Material.IRON_CHESTPLATE, 1, RewardType.TEAM, "Iron chestplate")
local blind_enemies = potion(Effect.BLINDNESS, 1, RewardType.ENEMY, "Blind your enemies", 1200)
local blindness = potion(Effect.BLINDNESS, 1, RewardType.TEAM, "Blind your team", 1200)
local gapple = item(Material.ENCHANTED_GOLDEN_APPLE, 1, RewardType.SOLO, "Gapple")
local diamond_chestplate = item(Material.DIAMOND_CHESTPLATE, 1, RewardType.SOLO, "Diamond chestplate")
local tnt = item(Material.TNT, 20, RewardType.SOLO, "20 tnt")
local glowing = potion(Effect.GLOWING, 1, RewardType.SOLO, "You're real bright")
local poison = potion(Effect.POISON, 1, RewardType.TEAM, "Poison", 1200)
local weakness = potion(Effect.WEAKNESS, 1, RewardType.TEAM, "Weakness")
local helium = potion(Effect.LEVITATION, 1, RewardType.SOLO, "Helium", 600)
local helium_enemies = potion(Effect.LEVITATION, 1, RewardType.ENEMY, "Levitation for enemies", 600)
local vex = action(RewardType.SOLO, "Vexs", spawn_vex)
local mutton = item(Material.COOKED_MUTTON, 15, RewardType.TEAM, "15 cooked mutton")
local bow = item(Material.BOW, 1, RewardType.TEAM, "Bow")
local arrows = item(Material.ARROW, 32, RewardType.TEAM, "Arrows")
local iron_leggings = item(Material.IRON_LEGGINGS, 1, RewardType.TEAM, "Iron leggings", {fire_protection=2})
local poison_enemies = potion(Effect.POISON, 1, RewardType.ENEMY, "Poison for enemies", 1200)
local saddle = item(Material.SADDLE, 1, RewardType.SOLO, "Saddle")
local turtle_helmet = item(Material.TURTLE_HELMET, 1, RewardType.SOLO, "Turtle helmet", {respiration=3, aqua_affinity=1})
local vex_enemies = action(RewardType.ENEMY, "Vexs for enemies", spawn_vex)
local porkchop = item(Material.COOKED_PORKCHOP, 15, RewardType.TEAM, "15 cooked porkchop")
local diamond_pickaxe = item(Material.DIAMOND_PICKAXE, 1, RewardType.SOLO, "Diamond pickaxe")
local diamond_axe = item(Material.DIAMOND_AXE, 1, RewardType.SOLO, "Diamond axe")
local boom = action(RewardType.SOLO, "You get boomed", smite)
local tp_tracked_player = action(RewardType.SOLO, "Swap with tracked player", tp_tracked)
local diamond_shovel = item(Material.DIAMOND_SHOVEL, 1, RewardType.TEAM, "Diamond shovel", {efficiency=4})
local jump = potion(Effect.JUMP_BOOST, 3, RewardType.SOLO, "Jump II")
local speed_I = potion(Effect.SPEED, 1, RewardType.SOLO, "Speed II")
local strength_I_team = potion(Effect.STRENGTH, 1, RewardType.TEAM, "Strength I for team")
local trident_loyalty = item(Material.TRIDENT, 1, RewardType.SOLO, "Loyalty trident", {loyalty=3, impaling=5})
local trident_riptide = item(Material.TRIDENT, 1, RewardType.SOLO, "Riptide trident", {riptide=3})
--local netherite_pickaxe = item(Material.NETHERITE_PICKAXE, 1, RewardType.SOLO, "NetheritePickaxe", {efficiency=3})
local night_vision = potion(Effect.NIGHT_VISION, 1, RewardType.SOLO, "Night vision")
--local resistance = potion(Effect.DAMAGE_RESISTANCE, 2, RewardType.SOLO, "Damage resistance", 600)
local haste = potion(Effect.HASTE, 3, RewardType.SOLO, "Haste III", 20 * 180)
local pearl = item(Material.ENDER_PEARL, 1, RewardType.SOLO, "Enderpearl")
local pearls = item(Material.ENDER_PEARL, 8, RewardType.SOLO, "Enderpearls")
local feather_boots = item(Material.CHAINMAIL_BOOTS, 1, RewardType.SOLO, "Feather falling boots", {feather_falling=4})
local frost_boots = item(Material.CHAINMAIL_BOOTS, 1, RewardType.SOLO, "Frostwalker boots", {frost_walker=2})
local iron_swift = item(Material.IRON_LEGGINGS, 1, RewardType.SOLO, "Swift sneak leggings", {swift_sneak=3})
local fire_resist = potion(Effect.FIRE_RESISTANCE, 1, RewardType.TEAM, "Fire resistance for team")
local water_breathing = potion(Effect.WATER_BREATHING, 1, RewardType.TEAM, "Water breathing for team")
local chorus_fruit = item(Material.CHORUS_FRUIT, 20, RewardType.SOLO, "Chorus fruit")
local bee_spawn = item(Material.BEE_SPAWN_EGG, 60, RewardType.SOLO, "60 Bee spawn eggs")
local invisibility = potion(Effect.INVISIBILITY, 1, RewardType.SOLO, "Invisibility")
local totem = item(Material.TOTEM_OF_UNDYING, 1, RewardType.SOLO, "Totem of undying")
local jukebox = item(Material.JUKEBOX, 1, RewardType.SOLO, "Jukebox")
local coal_enemies = item(Material.COAL, 32, RewardType.ENEMY, "Enemies get coal")

local stone_blocks = item(Material.STONE, 64, RewardType.TEAM, "Stone")
local sandstone_blocks = item(Material.SANDSTONE, 64, RewardType.TEAM, "Sandstone")
local redsandstone_blocks = item(Material.RED_SANDSTONE, 64, RewardType.TEAM, "Red sandstone")
local granite_blocks = item(Material.GRANITE, 64, RewardType.TEAM, "Granite")
local diorite_blocks = item(Material.DIORITE, 64, RewardType.TEAM, "Diorite")
local cobblestone_blocks = item(Material.COBBLESTONE, 64, RewardType.TEAM, "Cobblestone")
local endstone_blocks = item(Material.END_STONE, 64, RewardType.TEAM, "Endstone")
local calcite_blocks = item(Material.CALCITE, 64, RewardType.TEAM, "Calcite")
local oak_logs = item(Material.OAK_LOG, 64, RewardType.TEAM, "Oak logs")
local coal_blocks = item(Material.COAL_BLOCK, 64, RewardType.TEAM, "Coal Blocks")
local blocks = rewardChance("Blocks for team", {
                                                [stone_blocks]=2,
                                                [sandstone_blocks]=2,
                                                [redsandstone_blocks]=2,
                                                [granite_blocks]=2,
                                                [diorite_blocks]=2,
                                                [cobblestone_blocks]=3,
                                                [endstone_blocks]=2,
                                                [calcite_blocks]=2,
                                                [oak_logs]=1,
                                                [coal_blocks]=1})



--local slow_arrows = item(Material.ARROW, 1, RewardType.TEAM, "Slow arrows", {}, )
----

local golem_fish = shuffle({Material.SALMON, Material.COD})[1]

local village = {

                 hit(Entity.IRON_GOLEM, 1, "Slap an iron golem with " .. string.lower(golem_fish:name()), golem_fish)
                 :addPlayerPredicate(function (player)
                                              return inMainHand(player, golem_fish) end)
                 :setReward(rewards(slowness_II_enemies_temp, iron_pickaxe, haste)),

                 interactBlock(Material.BELL, 1, "Ring a bell with an iron hoe while food poisoned", Material.BELL)
                 :addPlayerPredicate(function (player)
                                              return inMainHand(player, Material.IRON_HOE) and hasPotionEffect(player, Effect.HUNGER) end)
                 :setReward(rewards(iron_pickaxe, speed_II_temp)),

                 tame(Entity.CAT, 1, "Tame a cat", Material.CAT_SPAWN_EGG)
                 :setReward(rewards(rabbit, blocks)),

                 doTimes(5, kill(Entity.VILLAGER, 1, "Kill 5 villagers with a bone", Material.IRON_SWORD))
                 :addPlayerPredicate(function (player)
                                              return inMainHand(player, Material.BONE) end)
                 :setReward(diamond_axe),

                 interactEntity(Entity.IRON_GOLEM, 1, "Repair an iron golem", Material.IRON_INGOT)
                 :addPlayerPredicate(function (player)
                                              return inMainHand(player, Material.IRON_INGOT) end)
                 :setReward(rewards(slowness_II_enemies_temp, chorus_fruit, haste))
                 }

local glow_oak = anyOf(Material.OAK_SIGN, 1, "Make an oak sign glow",

                                          interactBlock(Material.OAK_SIGN, 1, "", Material.OAK_SIGN)
                                          :addPlayerPredicate(glow_in_hand),
                                          interactBlock(Material.OAK_WALL_SIGN, 1, "", Material.OAK_SIGN)
                                          :addPlayerPredicate(glow_in_hand))
:setReward(slowness_II_enemies_temp)

local glow_birch = anyOf(Material.BIRCH_SIGN, 1, "Make a birch sign glow",

                                              interactBlock(Material.BIRCH_SIGN, 1, "", Material.BIRCH_SIGN)
                                              :addPlayerPredicate(glow_in_hand),
                                              interactBlock(Material.BIRCH_WALL_SIGN, 1, "", Material.BIRCH_SIGN)
                                              :addPlayerPredicate(glow_in_hand))
:setReward(slowness_II_enemies_temp)

local glow_spruce = anyOf(Material.SPRUCE_SIGN, 1, "Make a spruce sign glow",

                                                interactBlock(Material.SPRUCE_SIGN, 1, "", Material.BIRCH_SIGN)
                                                :addPlayerPredicate(glow_in_hand),
                                                interactBlock(Material.SPRUCE_WALL_SIGN, 1, "", Material.BIRCH_SIGN)
                                                :addPlayerPredicate(glow_in_hand))
:setReward(slowness_II_enemies_temp)

local glow_cherry = anyOf(Material.CHERRY_SIGN, 1, "Make a cherry sign glow",

                                                interactBlock(Material.CHERRY_SIGN, 1, "", Material.BIRCH_SIGN)
                                                :addPlayerPredicate(glow_in_hand),
                                                interactBlock(Material.CHERRY_WALL_SIGN, 1, "", Material.BIRCH_SIGN)
                                                :addPlayerPredicate(glow_in_hand))
:setReward(slowness_II_enemies_temp)

local glow_acacia = anyOf(Material.ACACIA_SIGN, 1, "Make an acacia sign glow",

                                                interactBlock(Material.ACACIA_SIGN, 1, "", Material.BIRCH_SIGN)
                                                :addPlayerPredicate(glow_in_hand),
                                                interactBlock(Material.ACACIA_WALL_SIGN, 1, "", Material.BIRCH_SIGN)
                                                :addPlayerPredicate(glow_in_hand))
:setReward(slowness_II_enemies_temp)

local signs = {}
if containsBiomeType(biomes, BiomeType.CHERRY) then
table.insert(signs, glow_cherry)
end

if containsBiomeType(biomes, BiomeType.OAK) then
table.insert(signs, glow_oak)
end

if containsBiomeType(biomes, BiomeType.BIRCH) then
table.insert(signs, glow_birch)
end

if containsBiomeType(biomes, BiomeType.SPRUCE) then
table.insert(signs, glow_spruce)
end

if containsBiomeType(biomes, BiomeType.SAVANNA) then
table.insert(signs, glow_acacia)
end

local wood = {

              structure(1, "Place a trapdoor on a composter", Material.COMPOSTER,
                         function (block) local below = block:getRelative(BlockFace.DOWN)
                         return MaterialType.TRAPDOOR:contains(block:getType()) and
                         below:getType() == Material.COMPOSTER  end)
              :setReward(rewards(slowness_II_enemies_temp, porkchop)),

              structure(1, "Place an iron trapdoor on a cauldron", Material.IRON_TRAPDOOR,
                         function (block) local below = block:getRelative(BlockFace.DOWN)
                         return block:getType() == Material.IRON_TRAPDOOR and
                         below:getType() == Material.CAULDRON  end)
              :setReward(rewards(slowness_II_enemies_temp, iron_chestplate)),

              cauldron(CauldronChange.ARMOR_WASH, 1, "Wash leather armor", Material.LEATHER_BOOTS)
              :setReward(rewards(slowness_II_enemies_temp, iron_chestplate))
              }

if signs[1] ~= nil then
table.insert(wood, shuffle(signs)[1])
end

local ingot = {
               obtain(Material.EMERALD, 1, "Obtain an emerald", Material.EMERALD)
               :setReward(rewards(blind_enemies, gapple)),

               anyOf(Material.DEEPSLATE_DIAMOND_ORE, 1, "Mine a diamond",
                                                     destroy(Material.DIAMOND_ORE, 1, "", Material.DIAMOND),
                                                     destroy(Material.DEEPSLATE_DIAMOND_ORE, 1, "", Material.DIAMOND))
               :addPlayerPredicate(function (player)
                                            return inMainHand(player, Material.DIAMOND_PICKAXE) or inMainHand(player, Material.NETHERITE_PICKAXE) or inMainHand(player, Material.IRON_PICKAXE) end)
               :setReward(rewards(blind_enemies, gapple)),

               obtain(Material.COPPER_INGOT, 1, "Obtain copper ingot", Material.COPPER_INGOT)
               :setReward(rewards(blind_enemies, gapple))
               }

local nether_mob = {
                    kill(Entity.PIGLIN_BRUTE, 1, "Kill a piglin brute", Material.GOLDEN_AXE)
                    :setReward(diamond_chestplate),

                    kill(Entity.GHAST, 1, "Kill a ghast", Material.GHAST_TEAR)
                    :setReward(tnt),

                    kill(Entity.WITHER_SKELETON, 1, "Kill a wither skeleton", Material.WITHER_SKELETON_SKULL)
                    :setReward(diamond_chestplate),

                    kill(Entity.BLAZE, 1, "Kill a blaze", Material.BLAZE_ROD)
                    :setReward(fire_resist)
                    }

local onSurfaceBottle = quest("org.bukkit.event.player.PlayerMoveEvent", 0, "Move on surface without bottle", Material.DIRT)
:addPlayerPredicate(function (player)
                             return onSurface(player) and not (inMainHand(player, Material.POTION) or inOffHand(player, Material.POTION)) end)
:setReward(action(RewardType.SOLO, "Catch fire", function (entity)
                                   entity:setFireTicks(20) end))

local rewardFire = rewardTask(RewardType.SOLO, "Can't go to surface without water bottle for 5 minutes", onSurfaceBottle)
rewardFire:addAction(function () rewardFire:getDelegate():unsubscribeAfterTime(3000) end, 3000)

local negative = {
                  damageByEntity(Entity.ARROW, DamageCause.PROJECTILE, -1, "Don't be shot by projectile", Material.ARROW)
                  :addEntityPredicate(function (entity)
                                               return isEntity(entity, Entity.ARROW) end)
                  :setReward(rewards(
                                      item(Material.BOW, 1, RewardType.ENEMY, "Bow + arrows for enemies", {power=2}),
                                          item(Material.ARROW, 32, RewardType.ENEMY, ""))),

                  pickup(Material.WHEAT_SEEDS, -1, "Don't pickup wheat seed", Material.WHEAT_SEEDS)
                  :setReward(glowing),

                  standOn(Material.COAL_ORE, -1, "Don't stand on coal ore", Material.COAL_ORE)
                  :setReward(rewards(coal_enemies, boom)),

                  standOn(Material.GRAVEL, -1, "Don't stand on gravel", Material.GRAVEL)
                  :setReward(poison),

                  hit(Entity.ZOMBIE, -1, "Don't hit a zombie", Material.ZOMBIE_HEAD)
                  :setReward(weakness),

                  takeDamage(DamageCause.FALL, -1, "Don't take fall damage", Material.CHAINMAIL_BOOTS)
                  :setReward(helium),

                  damageByEntity(Entity.CREEPER, DamageCause.ENTITY_EXPLOSION, -1, "Don't get bombed", Material.TNT)
                  :addEntityPredicate(function(entity)
                                              return isEntity(entity, Entity.CREEPER) end)
                  :setReward(vex),

                  eat(Material.BREAD, -1, "Don't eat bread", Material.BREAD)
                  :setReward(vex),

                  destroy(Material.CRAFTING_TABLE, -1, "Don't break crafting table", Material.CRAFTING_TABLE)
                  :setReward(weakness),

                  destroy(Material.DIRT, -1, "Don't break dirt", Material.DIRT)
                  :setReward(rewardFire),

                  anyOf(Material.TORCH, -1, "Don't place a torch",
                                        place(Material.TORCH, -1, "", Material.TORCH),
                                        place(Material.WALL_TORCH, -1, "", Material.TORCH))
                  :setReward(blindness)
                  }


local animal = {
                kill(Entity.BAT, 1, "Kill a bat", Material.BAT_SPAWN_EGG)
                :setReward(iron_swift),

                shear(Entity.SHEEP, 1, "Shear a blue sheep", Material.BLUE_WOOL)
                :addEntityPredicate(function (entity)
                                             return isEntity(entity, Entity.SHEEP) and DyeColor.BLUE == entity:getColor() end)
                :setReward(rewards(mutton, bee_spawn)),

                kill(Entity.SHEEP, 1, "Kill a yellow sheep with a brick", Material.YELLOW_WOOL)
                :addEntityPredicate(function (entity)
                                             return isEntity(entity, Entity.SHEEP) and DyeColor.YELLOW == entity:getColor() end)
                :addPlayerPredicate(function (player)
                                             return inMainHand(player, Material.BRICK) end)
                :setReward(rewards(mutton, bee_spawn)),

                doTimes(10, kill(Entity.CHICKEN, 1, "Kill 10 chicken", Material.FEATHER))
                :setReward(rewards(bow, arrows)),

                kill(Entity.PIG, 1, "Kill a flaming pig", Material.COOKED_PORKCHOP)
                :addEntityPredicate(onFire)
                :setReward(rewards(porkchop, iron_leggings)),

                doTimes(3, breed(Entity.COW, 1, "Breed 3 pairs of cows", Material.LEATHER))
                :setReward(poison_enemies),

                tame(Entity.HORSE, 1, "Tame a horse", Material.SADDLE)
                :setReward(saddle),

                tame(Entity.WOLF, 1, "Tame a wolf", Material.WOLF_SPAWN_EGG)
                :setReward(vex_enemies),

                destroy(Material.TURTLE_EGG, 1, "Break a turtle egg", Material.TURTLE_EGG)
                :setReward(turtle_helmet)
                }


local armadillo = interactEntity(Entity.ARMADILLO, 1, "Brush an armadillo", Material.ARMADILLO_SCUTE)
  :addPlayerPredicate(function (player) return inMainHand(player, Material.BRUSH) end)
      :setReward(invisibility)

if containsBiomeType(biomes, BiomeType.SAVANNA) or containsBiomeType(biomes, BiomeType.BADLANDS) then
table.insert(animal, armadillo)
end

local x_factor = {
                  obtain(Material.HEART_OF_THE_SEA, 1, "Find heart of the sea", Material.HEART_OF_THE_SEA)
                  :setReward(rewardChance("Diamond pickaxe or lightning",
                                           {
                                            [boom]=1,
                                            [diamond_pickaxe]=1
                                            }))
                  }

local clock_biome = shuffle(biomes:toArray())[1]

local weird_item = {
                    obtain(Material.MOSSY_STONE_BRICK_WALL, 1, "Obtain mossy stone brick wall", Material.MOSSY_STONE_BRICK_WALL)
                    :setReward(rewards(bow, arrows)),

                    place(Material.CRACKED_DEEPSLATE_TILES, 1, "Place cracked deepslate tiles", Material.CRACKED_DEEPSLATE_TILES)
                    :setReward(tp_tracked_player),

                    drop(Material.CLOCK, 1, "Drop a clock in " .. string.lower(clock_biome:name():gsub("_", " ")), Material.CLOCK)
                    :addPlayerPredicate(function (player)
                                                 return inBiome(player, clock_biome)
                                                 end)
                    :setReward(tp_tracked_player),

                    eat(Material.GOLDEN_APPLE, 1, "Eat a golden apple", Material.GOLDEN_APPLE)
                    :setReward(swap_stick),

                    obtainAny(MaterialType.DISC, 1, "Obtain a music disc", Material.MUSIC_DISC_STAL)
                    :setReward(rewards(vex_enemies, blocks, jukebox))
                    }

local day_night = {
                   doTimes(20, anyOf(Material.SEAGRASS, 1, "During the night, destroy 20 seagrass",
                                                        destroy(Material.SEAGRASS, 1, "", Material.SEAGRASS),
                                                        destroy(Material.TALL_SEAGRASS, 1, "", Material.SEAGRASS)))
                   :addPlayerPredicate(function (player)
                                                return not isDay(player) end)
                   :setReward(rewards(blind_enemies, diamond_shovel)),

                   allOf(Material.RED_STAINED_GLASS, 1, "Place 5 red glass during the day and 5 black glass during the night",
                                                     doTimes(5, place(Material.RED_STAINED_GLASS, 1, "", Material.DIRT))
                                                     :addPlayerPredicate(isDay),
                                                     doTimes(5, place(Material.BLACK_STAINED_GLASS, 1, "", Material.DIRT))
                                                     :addPlayerPredicate(function (player)
                                                                                  return not isDay(player) end))
                   :setReward(rewards(vex_enemies, diamond_shovel))
                   }

local weird_food = {
                    place(Material.CAKE, 1, "Place a cake", Material.CAKE)
                    :setReward(speed_I),

                    eat(Material.HONEY_BOTTLE, 1, "Drink honey", Material.HONEY_BOTTLE)
                    :setReward(swap_glass_bottle),

                    eat(Material.PUMPKIN_PIE, 1, "Eat pumpkin pie", Material.PUMPKIN_PIE)
                    :setReward(speed_I)
                    }

local rare_mob = {
                  kill(Entity.WITCH, 1, "Kill a witch", Material.CAULDRON)
                  :setReward(strength_I_team)
                  }

local grind = {
               standOn(Material.BEDROCK, 1, "Stand on bedrock", Material.BEDROCK)
               :setReward(helium_enemies),

               playerState(1, "Reach build height", Material.NETHER_STAR,
                       function (player) return aboveY(player, player:getWorld():getMaxHeight())  end)
               :setReward(rewards(feather_boots, pearl)),

               damageByEntity(Entity.FALLING_BLOCK, DamageCause.FALLING_BLOCK, 1, "Get crushed by anvil", Material.ANVIL)
               :setReward(rewards(blocks, totem)),

               quest("org.bukkit.event.entity.FoodLevelChangeEvent", 1, "Begin to starve", Material.WITHER_SKELETON_SKULL)
               :addPlayerPredicate(function (player)
                                            return (player:getFoodLevel() == 0) end)
               :setReward(porkchop),

               quest("org.bukkit.event.player.PlayerLevelChangeEvent", 1, "Reach level 15", Material.EXPERIENCE_BOTTLE)
               :addPlayerPredicate(function (player)
                                            return (player:getLevel() >= 15) end)
               :setReward(rewards(night_vision, totem))
               }

local fish = {
              eat(Material.PUFFERFISH, 1, "Eat a pufferfish", Material.PUFFERFISH)
              :setReward(night_vision),

              bucket(Entity.AXOLOTL, 1, "Bucket an axolotl", Material.AXOLOTL_BUCKET)
              :setReward(night_vision)
              }

local grass = {
               pickup(Material.GRASS_BLOCK, 1, "Pickup a grass block", Material.GRASS_BLOCK)
               :setReward(rewards(pearls, blocks))
               }

local desert_tasks = {
                      eat(Material.RABBIT_STEW, 1, "Eat rabbit stew", Material.RABBIT_STEW)
                      :setReward(speed_II_temp),

                      doTimes(3, kill(Entity.HUSK, 1, "Kill 3 husks", Material.HUSK_SPAWN_EGG))
                      :setReward(speed_I),

                      interactBlock(Material.FLOWER_POT, 1, "Pot a cactus", Material.FLOWER_POT)
                      :addPlayerPredicate(function (player)
                                                   return inMainHand(player, Material.CACTUS) end)
                      :setReward(night_vision)
                      }

local snow_tasks = {
                    shear(Entity.SNOWMAN, 1, "Shear a snow golem", Material.CARVED_PUMPKIN)
                    :setReward(frost_boots)
                    }

local ocean_tasks = {
                     place(Material.DECORATED_POT, 1, "Place decorated pot", Material.DECORATED_POT)
                     :setReward(water_breathing),

                     kill(Entity.DROWNED, 1, "Kill a trident wielding drowned", Material.TRIDENT)
                     :addEntityPredicate(function (entity)
                                                  return inMainHand(entity, Material.TRIDENT) end)
                     :setReward(rewardChance("Trident",
                                              {
                                               [trident_loyalty]=1,
                                               [trident_riptide]=1
                                               })),

                     interactEntity(Entity.SQUID, 1, "Attempt to milk a squid while having dolphin's grace", Material.INK_SAC)
                     :addPlayerPredicate(function (player)
                                                  return inMainHand(player, Material.BUCKET) and hasPotionEffect(player, Effect.DOLPHINS_GRACE) end)
                     :setReward(strength_I_team)
                     }

local jungle_tasks = {
                      interactEntity(Entity.PARROT, 1, "Feed a parrot a cookie", Material.COOKIE)
                      :addPlayerPredicate(function (player)
                                                   return inMainHand(player, Material.COOKIE) end)
                      :setReward(speed_I),

                      kill(Entity.SPIDER, 1, "Kill a spider in a jungle", Material.SPIDER_EYE)
                      :addPlayerPredicate(function (player)
                                                   return inBiomeType(player, BiomeType.JUNGLE) end)
                      :addEntityPredicate(function (entity)
                                                   return inBiomeType(entity, BiomeType.JUNGLE) end)
                      :setReward(speed_I)
                      }

local snow_jungle = {
                     destroy(Material.MELON, 1, "Break a melon in a snow biome", Material.MELON)
                     :addPlayerPredicate(function (player)
                                                  return inBiomeType(player, BiomeType.SNOW) end)
                     :setReward(strength_I_team)
                     }

local nether = {
                kill(Entity.GHAST, 1, "Kill a ghast", Material.GHAST_TEAR)
                :setReward(tnt),

                obtain(Material.COMPARATOR, 1, "Obtain redstone comparator", Material.COMPARATOR)
                :setReward(strength_I_team),

                doTimes(15, kill(Entity.ZOMBIFIED_PIGLIN, 1, "Kill 15 zombie pigmen", Material.GOLDEN_SWORD))
                :setReward(rewards(strength_I_team, diamond_chestplate)),

                obtain(Material.REDSTONE_LAMP, 1, "Obtain redstone lamp", Material.REDSTONE_LAMP)
                :setReward(speed_I)
                }
----
----
if containsBiomeType(biomes, BiomeType.SNOW) then
take(1, shuffle(snow_tasks))
end

if containsBiomeType(biomes, BiomeType.OCEAN) then
take(1, shuffle(ocean_tasks))
end

if containsBiomeType(biomes, BiomeType.JUNGLE) then
take(2, shuffle(jungle_tasks))
end

if containsBiomeType(biomes, BiomeType.DESERT) then
take(1, shuffle(desert_tasks))
end

if containsBiomeType(biomes, BiomeType.JUNGLE) and containsBiomeType(biomes, BiomeType.SNOW) then
take(1, shuffle(snow_jungle))
end
----
local tasks = {
               village, wood, ingot, animal, weird_item, x_factor, day_night, weird_food,
               rare_mob, grind, fish, grass, nether
               }

-- Paper only
local left = kill(Entity.SKELETON, 1, "Kill a lefty skeleton", Material.BONE)
:addEntityPredicate(function (entity)
                             return isEntity(entity, Entity.SKELETON) and entity:isLeftHanded() end)
:setReward(water_breathing)

if string.find(Bukkit:getVersion(), "Paper") then
table.insert(rare_mob, left)
end

take(2, shuffle(negative))
take(27 - taskCount(), prepare_tasks(tasks))

addTieBreaker(pvp(1, "Kill an enemy player", Material.DIAMOND_SWORD))
