impl = _require("reward/impl")
reward_type = _require("constants").reward_types

local reward = {}

reward.solo = {}
reward.team = {}
reward.enemy = {}

reward.solo.give_item = impl._give_item(reward_type.solo)
reward.team.give_item = impl._give_item(reward_type.team)
reward.enemy.give_item = impl._give_item(reward_type.enemy)

reward.solo.apply_potion = impl._apply_potion(reward_type.solo)
reward.team.apply_potion = impl._apply_potion(reward_type.team)
reward.enemy.apply_potion = impl._apply_potion(reward_type.enemy)

reward.solo.run_action = impl._run_action(reward_type.solo)
reward.team.run_action = impl._run_action(reward_type.team)
reward.enemy.run_action = impl._run_action(reward_type.enemy)

reward.solo.subscribe_task = impl._subscribe_task(reward_type.solo)
reward.team.subscribe_task = impl._subscribe_task(reward_type.team)
reward.enemy.subscribe_task = impl._subscribe_task(reward_type.enemy)

function reward.combine(rewards_table)
   if type(rewards_table) ~= "table" then
      error("reward.combine requires a table (list) of reward objects as its argument, got " .. type(rewards_table), 2)
   end
   for i, v in ipairs(rewards_table) do
      if type(v) ~= "userdata" then
	 error("Item at index " .. i .. " in reward.combine is not userdata, type: " .. type(v), 2)
      end
   end

   local success, composite_reward_or_err = pcall(_rewards, rewards_table)

   if not success then
      error("reward.combine Failed to create RewardComposite object: " .. tostring(composite_reward_or_err), 2)
   end

   return composite_reward_or_err
end

function reward.chance(description, weighted_rewards_table)
   if type(description) ~= "string" or description == "" then
      error("reward.chance requires a non-empty string description as the first argument", 2)
   end
   
   if type(weighted_rewards_table) ~= "table" or #weighted_rewards_table == 0 then
      error("reward.chance requires a non-empty table (list) of weighted rewards as the second argument", 2)
   end

   for i, item in ipairs(weighted_rewards_table) do
      if type(item) ~= "table" or not item.reward or type(item.weight) ~= "number" or item.weight <= 0 then
	 error("Invalid item structure or weight at index " .. i .. " in weighted_rewards_table for rewards.chance. Expected { reward = RewardComponent, weight = positive_integer }.", 2)
      end
   end

   local success, chance_reward_or_err = pcall(_rewardChance, description, weighted_rewards_table)

   if not success then
      error("reward.chance Failed to create RewardChance object: " .. tostring(chance_reward_or_err), 2)
   end

   return chance_reward_or_err
end

return reward
