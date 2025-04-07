const = _require("constants")

local factory = {}

function factory._create_task_binding(table_id, task_obj_f)
   return function(args)
      if type(args) ~= "table" then
	 error(table_id .. " requires a table argument { target=..., (optional) description=..., (optional) display=... }", 2)
      end

      local target_entity = args.target
      local description = args.description or const.default_description
      local display_material = args.display or const.default_display
      if not target_entity or type(description) ~= "string" or not display_material then
	 error(table_id .. " missing required keys: target (entity_type constant), (optional) description (string), (optional) display (material constant)", 2)
      end

      if type(target_entity) ~= "userdata" and type(target_entity) ~= "table" then error(table_id .." 'target' should be of type constants.entities", 2) end
      if type(display_material) ~= "userdata" then error(table_id .. " 'display' should be of type constants.materials", 2) end

      local value = args.value or const.default_value -- Default value is 1 point
      local reward = args.reward -- Defaults to nil if not present
      local p_condition = args.player_condition -- Defaults to nil
      local e_condition = args.entity_condition -- Defaults to nil

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

      if not task_obj then error("Failed to create internal task object for " .. table_id, 2) end

      -- Configure task object
      if p_condition then task_obj:addPlayerCondition(p_condition) end
      if e_condition then task_obj:addEntityCondition(e_condition) end
      if b_condition then task_obj:addBlockCondition(b_condition) end
      if reward then task_obj:setReward(reward) end

      return task_obj
   end
end

return factory
