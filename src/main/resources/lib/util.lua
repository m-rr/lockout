local util = {
   entity = _require("util/entity")
}

function util._get_opt(tbl, key, default_value)
   return tbl[key] ~= nil and tbl[key] or default_value
end

function util.shuffle(tbl)
   for i = #tbl, 2, -1 do
      local j = math.random(i)
      tbl[i], tbl[j] = tbl[j], tbl[i]
   end
   return tbl
end

function util.take(n, tbl)
   if type(tbl) ~= "table" or type(n) ~= "number" then error("util.take requires exactly two arguments: n (number), tbl (table list)", 2) end
   if n > #tbl then error("util.take 'n' : " .. tostring(n) .. " must be smaller than the size of 'tbl' : " .. tostring(#tbl), 2) end
   
   local selection = {}
   for i=1, n do
      table.insert(selection, tbl[i])
   end
   return selection
end

function util.random_choice(tbl)
   if type(tbl) ~= "table" then error("util.random_choice requires exactly one argument: tbl (table list)", 2) end
   local index = math.random(#tbl)
   return tbl[index]
end

function util.get_spawn_location(world_name)
   if type(world_name) ~= "string" then
      error("util.get_spawn_location requires a string argument: 'world_name'", 2)
   end
   local world = _getWorld(world_name)
   return world:getSpawnLocation();
end

function util.get_world_time(world_name)
   if type(world_name) ~= "string" then
      error("util.get_world_time requires a string argument: 'world_name'", 2)
   end
   local world = _getWorld(world_name)
   return world:getTime()
end

function util.get_elapsed_game_seconds()
   return _getTimer():elapsedTime()
end

function util.broadcast(msg)
   if type(msg) ~= "string" then
      error("You can only broadcast a string!", 2)
   end
   log.sendAllChat(msg)
   log.consoleLog("broadcast: " .. msg)
end

function util.location(args)
   if type(args) ~= "table" then
      error("util.location requires table arguments: {x=..., y=..., z=...,world=...")
   end

   local x = args.x
   local y = args.y
   local z = args.z

   if type(x) ~= "number" or
   type(y) ~= "number" or type(z) ~= "number" then
      error("util.location coordinates must be numbers.", 2)
   end

   local world = args.world

   return _createLoc(world, x, y, z)
end

function util.get_nearby_biomes(location, radius)
   return _getBiomes(location, radius)
end

local _list_contains = function(list_table, item_to_find)
   if type(list_table) ~= "table" then
      return false
   end

   for _,value in ipairs(list_table) do
      if value == item_to_find then
         return true
      end
   end
   return false
end

local filter_task = function(task_tbl, filter_tbl)
   local tags = task_tbl.tags
   for _,filter_term in ipairs(filter_tbl) do
      if _list_contains(tags, filter_term) then
         return true
      end
   end
   return false
end

function util.filter_pool(pool, filter_tbl)
   local filtered_tasks = {}
   if type(pool) ~= "table" then
      error("util.filter_pool requires table arguments 'pool', 'filter_tbl'", 2)
   end

   for _,value in ipairs(pool) do
      if filter_task(value, filter_tbl) then
         table.insert(filtered_tasks, value)
      end
   end

   return filtered_tasks
end

-- Takes the information in pool_source and adds tasks to result_tbl
local add_pool_source = function(pool_source, total_count, result_tbl)
   local source = pool_source.source
   local count = pool_source.count or 0
   local do_shuffle = pool_source.shuffle or false
   local take_remaining = pool_source.take_remaining

   if #result_tbl >= total_count then
      return
   end

   if do_shuffle then
      source = util.shuffle(source)
   end

   local remaining = total_count - #result_tbl

   -- Make sure to not take too many
   local take_amount = count
   if take_remaining then
      take_amount = remaining
   else
      take_amount = math.min(count, remaining)
   end
   -- take the correct amount
   source = util.take(take_amount, source)

   for _,value in ipairs(source) do
      result_tbl[#result_tbl + 1] = value.task
   end

end

function util.select_tasks(config)
   if type(config) ~= "table" then
      error("util.select_tasks requires table arguments: {total=..., pools=...}", 2)
   end

   local total = config.total
   if type(total) ~= "number" then
      error("util.select_tasks argument 'total' must be a number", 2)
   end

   local pools = config.pools
   if type(pools) ~= "table" then
      error("util.select_tasks argument 'pools' must be a table", 2)
   end

   local selected_tasks = {}

   for _,pool in ipairs(pools) do
      add_pool_source(pool, total, selected_tasks)
   end

   return selected_tasks
end

return util
