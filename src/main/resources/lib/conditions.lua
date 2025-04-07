util = _require("util/entity.lua")

local conditions = {}

function conditions.in_main_hand(entity, mat)
   return util.get_main_hand(entity) == mat
end

function conditions.in_off_hand(entity, mat)
    return util.get_off_hand(entity) == mat
end

function conditions.in_biome(entity, biome)
    return util.get_biome(entity) == biome
end

function conditions.in_biome_type(entity, biomeType)
    return biomeType:contains(util.get_biome(entity))
end

function conditions.on_block(entity, mat)
    return entity:getLocation():getBlock():getRelative(BlockFace.DOWN):getType() == mat
end

function conditions.above_y(entity, y)
    return entity:getLocation():getY() >= y
end

function conditions.has_potion_effect(entity, potionEffectType)
    return entity:getPotionEffect(potionEffectType:getEffect()) ~= nil
end

function conditions.on_fire(entity)
    return entity:getFireTicks() > 0
end

function conditions.in_water(entity)
    return entity:isInWater()
end

function conditions.is_day(entity)
   local time = entity:getWorld():getTime()
   return time < 13000 and time > 1000
end

function conditions.is_night(entity)
   return not entity.is_day(entity)
end

function conditions.on_surface(entity)
    return entity:getLocation():getBlock():getLightFromSky() == 15
end

function conditions.is_entity(entity, targetEntityType)
    return entity:getType() == targetEntityType
end

function conditions.holding_item(material)
    return function(ent) return entity.in_main_hand(ent, material) end
end

function conditions.all_of(...)
  local predicates = {...} -- Capture all passed functions
  if #predicates == 0 then
    -- Decide behavior for no predicates (e.g., always true?)
    return function() return true end
  end

  return function(value) -- Return the *new* composed predicate function
    for i = 1, #predicates do
      if not predicates[i](value) then
        return false -- Short-circuit: if one is false, the AND result is false
      end
    end
    return true -- All predicates returned true
  end
end

function conditions.any_of(...)
  local predicates = {...} -- Capture all passed functions
   if #predicates == 0 then
    -- Decide behavior for no predicates (e.g., always false?)
    return function() return false end
  end

  return function(value) -- Return the *new* composed predicate function
    for i = 1, #predicates do
      if predicates[i](value) then
        return true -- Short-circuit: if one is true, the OR result is true
      end
    end
    return false -- None of the predicates returned true
  end
end

function conditions.negate(pred)
   return function(value) return not pred(value) end
end

return conditions
