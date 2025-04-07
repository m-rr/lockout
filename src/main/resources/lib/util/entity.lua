entity = {}

entity.get_main_hand = function(entity)
    return entity:getEquipment():getItemInMainHand():getType()
end

entity.get_off_hand = function (entity)
    return entity:getEquipment():getItemInOffHand():getType()
end

entity.get_biome = function(entity)
    return entity:getLocation():getBlock():getBiome()
end

return entity
