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

return reward
