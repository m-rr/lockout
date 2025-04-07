--[[
   These create TaskComponent's which can be composed together in any combination


      -- Create TaskComponent object
      local task_obj = nil
      if type(target_entity) == "table" then -- If a table is provided for 'target', then assume it is a TaskChoice
	 local task_objs = {}
	 for i = 1,#target_entity do
	    table.insert(task_objs, task_obj_f(target_entity[i], value, description, display_material))
	 end
	 task_obj = _anyOf(display_material, value, description, table.unpack(task_objs))
      else -- Otherwise it is just a single entity
	 task_obj = task_obj_f(target_entity, value, description, display_material)	 
      end
   	 error(table_id .. " missing required keys: target (entity_type constant), (optional) description (string), (optional) display (material constants)", 2)
]]--

const = _require("constants")

local composites = {}

function composites._impl(table_id, task_obj_f)
   return function(args)
      if type(args) ~= "table" then error(table_id .. " requires a table argument: {tasks=...}", 2) end

      local tasks = args.tasks
      local value = args.value or const.default_value
      local description = args.description or const.default_description
      local display = args.display or const.default_display
      if type(tasks) ~= "table" or type(value) ~= "number" or type(description) ~= "string" or type(display) ~= "userdata" then
	 error(table_id .. " missing required keys: tasks (table list), (optional) description (string), (optional) display (material constants), (optional) value (number)", 2)
      end

      local success, task_obj_or_err = pcall(task_obj_f, display, value, description, table.unpack(tasks))
      if not success then error(table_id .. " failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

      local task_obj = task_obj_or_err

      -- These may or may not work
      local reward = args.reward
      local p_condition = args.player_condition
      if reward then task_obj:setReward(reward) end
      if p_condition then task_obj:addPlayerCondition(p_condition) end
      
      return task_obj
   end
end


function composites._do_times(args)
   if type(args) ~= "table" then error("task.do_times requires a table argument: {times=..., target_task=...}", 2) end

   local times = args.times
   local target_task = args.target_task
   if type(times) ~= "number" or type(target_task) ~= "userdata" then
      error("task.do_times missing required keys: times (number), target_tasks (table list)", 2)
   end

   local success, task_obj_or_err = pcall(_doTimes, times, target_task)
   if not success then error("task.do_times failed to create TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err

   -- These may or may not work
   local reward = args.reward
   local p_condition = args.player_condition
   if reward then task_obj:setReward(reward) end
   if p_condition then task_obj:addPlayerCondition(p_condition) end

   return task_obj
end


return composites
