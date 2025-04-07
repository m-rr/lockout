const = _require("constants")
composites = _require("task/composite")

--[[
 Helper functions to create configured TaskComponent objects using named arguments.
 These functions typically return the task object, which should then be added to the
 LockoutBoard.Tasks or LockoutBoard.Tiebreakers table by the script writer.
]]--

local task = {
   entity = _require("task/entity"),
   block = _require("task/block"),
   item = _require("task/item"),
   any_of = composites._impl("task.any_of", _anyOf),
   all_of = composites._impl("task.all_of", _allOf),
   sequential = composites._impl("task.sequential", _sequential),
   do_times = composites._do_times
}

function task.advancement(args)
   if type(args) ~= "table" then
      error("task.advancement requires a table argument { goal=..., (optional) description=..., (optional) value, (optional) display=... }", 2)
   end

   local goal = args.goal -- 'goal' is the case-insensitive name of the advancement. Example: "ice bucket challenge"
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   local value = args.value or const.default_value
   if type(goal) ~= "string" or type(display) ~= "userdata" or type(description) ~= "string" then
      error("task.advancement missing required keys: goal (string), (optional) description (string), (optional) value (string), (optional) display (material constant)", 2)
   end

   local task_obj = _advancement(goal, value, description, display)
   local success, task_obj_or_err = pcall(_advancement, goal, value, description, display)
   if not success then error("task.advancement Failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end
   local task_obj = task_obj_or_err
   
   local reward = args.reward
   local p_condition = args.player_condition
   
   -- Configure task object
   if p_condition then task_obj:addPlayerCondition(p_condition) end
   if reward then task_obj:setReward(reward) end

   return task_obj
end


function task.structure(args)
   if type(args) ~= "table" then
      error("task.structure requires a table argument: { block_condition=..., }", 2)
   end

   local b_func = args.block_condition
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   local value = args.value or const.default_value
   if type(b_func) ~= "function" or type(display) ~= "userdata" or type(description) ~= "string" then
      error("task.structure missing required keys: block_condition (function(block) return boolean), (optional) description (string), (optional) value (string), (optional) display (material constant)", 2)
   end

   local success, task_obj_or_err = pcall(_structure, value, description, display, b_func)
   if not success then error("task.structure Failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err
   
   local reward = args.reward
   local p_condition = args.player_condition

   -- Configure task object
   if p_condition then task_obj:addPlayerCondition(p_condition) end
   if reward then task_obj:setReward(reward) end

   return task_obj
end

function task.pvp(args)
   if type(args) ~= "table" then
      error("task.pvp requires a table argument: {}", 2)
   end

   local value = args.value or const.default_value
   if type(value) ~= "number" then error("task.pvp key 'value' must be a number", 2) end
   local description = args.description or const.default_description
   if type(description) ~= "string" then error("task.pvp key 'description' must be a string", 2) end
   local display = args.display or const.default_display
   if type(display) ~= "userdata" then error("task.pvp key 'display' must be of type constants.materials", 2) end

   local success, task_obj_or_err = pcall(_pvp, value, description, display)
   if not success then error("task.pvp failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err

   local reward = args.reward
   local p_condition = args.player_condition
   local t_p_condition = args.target_player_condition

   -- Configure task obj
   if reward then tasj_obj:setReward(reward) end
   if p_condition then task_obj:addPlayerCondition(p_condition) end
   if t_p_condition then task_obj:addTargetPlayerCondition(t_p_condition) end

   return task_obj
end

function task.take_damage(args)
   if type(args) ~= "table" then error("task.take_damage requires a table argument: {damage_cause=...}", 2) end

   local damage_cause = args.damage_cause
   local value = args.value or const.default_value
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   if type(damage_cause) ~= "userdata" or type(description) ~= "string"
      or type(display) ~= "userdata" or type(value) ~= "number" then
      error("task.take_damage missing required keys: damage_cause (constants damage_causes), (optional) description (string), (optional) display (const materials), (optional) value (number)", 2)
   end

   local success, task_obj_or_err = pcall(_takeDamage, damage_cause, value, description, display)
   if not success then error("task.damage_cause failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err
   
   local reward = args.reward
   local p_condition = args.player_condition

   if reward then task_obj:setReward(reward) end
   if p_condition then task_obj:addPlayerCondition(p_condition) end

   return task_obj
end

function task.damage_by_block(args)
   if type(args) ~= "table" then error("task.damage_by_block requires a table argument: {target=...}", 2) end

   local block_mat = args.target
   local damage_cause = args.damage_cause
   local value = args.value or const.default_value
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   if type(block_mat) ~= "userdata" or type(description) ~= "string"
      or type(display) ~= "userdata" or type(value) ~= "number" then
      error("task.damage_by_block missing required keys: target (constants materials), damage_cause (constants damage_causes) (optional) description (string), (optional) display (const materials), (optional) value (number)", 2)
   end

   local success, task_obj_or_err = pcall(_damageByBlock, block_mat, damage_cause, value, description, display)
   if not success then error("task.damage_by_block failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err
   
   local reward = args.reward
   local p_condition = args.player_condition
   local b_condition = args.block_condition
   if reward then task_obj:setReward(reward) end
   if p_condition then task_obj:addPlayerCondition(p_condition) end
   if b_condition then task_obj:addBlockCondition(b_condition) end

   return task_obj
end

function task.damage_by_entity(args)
   if type(args) ~= "table" then error("task.damage_by_entity requires a table argument: {target=...}", 2) end

   local ent_type = args.target
   local damage_cause = args.damage_cause
   local value = args.value or const.default_value
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   if type(ent_type) ~= "userdata" or type(description) ~= "string"
      or type(display) ~= "userdata" or type(value) ~= "number" then
      error("task.damage_by_entity missing required keys: target (constants materials), damage_cause (constants damage_causes) (optional) description (string), (optional) display (const materials), (optional) value (number)", 2)
   end

   local success, task_obj_or_err = pcall(_damageByEntity, ent_type, damage_cause, value, description, display)
   if not success then error("task.damage_by_entity failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err
   
   local reward = args.reward
   local p_condition = args.player_condition
   local e_condition = args.entity_condition
   if reward then task_obj:setReward(reward) end
   if p_condition then task_obj:addPlayerCondition(p_condition) end
   if e_condition then task_obj:addEntityCondition(e_condition) end

   return task_obj
end

function task.potion_effect(args)
   if type(args) ~= "table" then error("task.potion_effect requires a table argument: {potion_effect=...}", 2) end

   local potion_effect = args.potion_effect
   local value = args.value or const.default_value
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   if type(potion_effect) ~= "userdata" or type(description) ~= "string"
      or type(display) ~= "userdata" or type(value) ~= "number" then
      error("task.potion_effect missing required keys: potion_effect (constants effects), (optional) description (string), (optional) display (constants materials), (optional) value (number)", 2)
   end

   local success, task_obj_or_error = pcall(_getEffect, potion_effect, value, description, display)
   if not success then error("task.potion_effect failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_error
   
   local reward = args.reward
   local p_condition = args.player_condition
   if reward then task_obj:setReward(reward) end
   if p_condition then task_obj:addPlayerCondition(p_condition) end

   return task_obj
end

function task.player_state(args)
   if type(args) ~= "table" then error("task.player_state requires a table argument: {player_condition=...}", 2) end

   -- In this case, a player condition is required
   local p_condition = args.player_condition
   local value = args.value or const.default_value
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   if type(p_condition) ~= "function" or type(description) ~= "string"
      or type(display) ~= "userdata" or type(value) ~= "number" then
      error("task.player_state missing required keys: player_condition (function(player) return boolean), (optional) description (string),(optional) display (constants materials), (optional) value (number)", 2)
   end

   local success, task_obj_or_err = pcall(_playerState, value, description, display, p_condition)
   if not success then error("task.player_state failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err

   local reward = args.reward
   if reward then task_obj:setReward(reward) end

   return task_obj
end

-- You should avoid using this unless neccessary
function task.quest(args)
   if type(args) ~= "table" then error("task.player_state requires a table argument: {event_name=...}", 2) end

   local event_name = args.event_name
   local value = args.value or const.default_value
   local description = args.description or const.default_description
   local display = args.display or const.default_display
   if type(event_name) ~= "string" or type(description) ~= "string"
      or type(display) ~= "userdata" or type(value) ~= "number" then
      error("task.quest missing required keys: event_name (string), (optional) description (string),(optional) display (constants materials), (optional) value (number)", 2)
   end

   local success, task_obj_or_err = pcall(_quest, event_name, value, description, display)
   if not success then error("task.quest failed to create internal TaskComponent: " .. tostring(task_obj_or_err), 2) end

   local task_obj = task_obj_or_err
   
   local reward = args.reward
   local p_conditon = args.player_condition
   if reward then task_obj:setReward(reward) end
   if p_condition then task_obj:addPlayercondition(p_condition) end

   return task_obj
end

return task
