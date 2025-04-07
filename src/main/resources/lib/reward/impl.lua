local const = _require("constants")

impl = {}

function impl._give_item(reward_type)
   return function(args)
      if type(args) ~= "table" then
	 error("reward.<target>.give_item requires a table argument {item=..., (optional) amount=..., (optional) enchantments=..., (optional) delay_ticks=...}", 2)
      end
      
      local item_type = args.item
      local name = args.description or const.default_description
      if not item_type or type(name) ~= "string" then
	 error("reward.<target>.give_item missing required keys: item (materials constants), description (string)", 2)
      end
      
      if type(item_type) ~= "userdata" then error("reward.<target>.give_item 'item' should be of type constants.materials", 2) end

      local amount = args.amount or 1 -- Default amount is 1
      local delay = args.delay_ticks or 0 -- Default is no delay
      local enchants = args.enchantments
      if not enchants then
	 return _item(item_type, amount, reward_type, name):setDelay(delay)
      elseif type(enchants) == "table" then
	 return _item(item_type, amount, reward_type, name, enchants):setDelay(delay)
      else
	 error("reward.<target>.give_item 'enchantments' must be of type 'table' (key constants.enchantments), (value level)", 2)
      end
      
   end
end

function impl._apply_potion(reward_type)
   return function(args)
      if type(args) ~= "table" then
	 error("reward.<target>.apply_potion requires a table argument {effect=..., (optional) description=..., (optional) level=..., (optional) duration_ticks=...", 2)
      end

      local potion_type = args.effect
      local name = args.description or const.default_description
      if not potion_type or type(name) ~= "string" then
	 error("reward.<target>.apply_potion missing required keys: effect (effects constants)", 2)
      end

      if type(potion_type) ~= "userdata" then error("reward.<target>.apply_potion 'effect' should be of type constants.effects", 2) end

      local duration_ticks = args.duration_ticks or const.default_potion_ticks
      local level = args.level or 1 -- A potion should have a level of 1 by default
      local delay = args.delay_ticks or 0 -- Default is no delay
      return _potion(potion_type, level, reward_type, name, duration_ticks):setDelay(delay)
   end
end

function impl._run_action(reward_type)
   return function(args)
      if type(args) ~= "table" then
	 error("reward.<target>.run_action requires a table argument {action=..., (optional) description=..., (optional) delay_ticks=...}", 2)
      end

      local action = args.action
      local name = args.description or const.default_description
      if not action or type(name) ~= "string" then
	 error("reward.<target>.run_action missing required keys: action (function(player) no return)", 2)
      end

      if type(action) ~= "function" then error("reward.<target>.run_action 'action' should be a function taking a player argument", 2) end

      local delay = args.delay_ticks or 0 -- Default is no delay
      
      return _action(reward_type, name, action):setDelay(delay)
   end
end

local function unsub_player_from_task(task_action)
   return function(player)
      
   end
end

function impl._subscribe_task(reward_type)
   return function(args)
      if type(args) ~= "table" then
	 error("reward.<target>.subscribe_task requires a table argument {hidden_task=..., (optional) description=..., (optional) until_seconds=..., (optional) delay_ticks=...}", 2)
      end

      local hidden_task = args.hidden_task
      local name = args.description or const.default_description
      if not hidden_task or type(name) ~= "string" then
	 error("reward.<target>.subscribe_task missing required keys: hidden_task (task)", 2)
      end

      if type(hidden_task) ~= "userdata" then error("reward.<target>.subscribe_task 'hidden_task' should be of type task.*", 2) end

      local delay = args.delay_ticks or 0 -- Default is no delay

      -- Create RewardComponent object
      local reward_obj = _rewardTask(reward_type, name, hidden_task):setDelay(delay)
      
      local subscribe_time = args.until_seconds
      if type(subscribe_time) == "number" then
	 reward_obj:getDelegate():setActiveSubscriptionTimeSeconds(subscribe_time)
      end
      
      return reward_obj
      
   end
end

return impl
