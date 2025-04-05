local create_task_binding = _require("task/factory")._create_task_binding

local block = {}

block.place = create_task_binding("task.block.place", _place)
block.destroy = create_task_binding("task.block.destroy", _destroy)
block.stand_on = create_task_binding("task.block.stand_on", _standOn)
block.interact = create_task_binding("task.block.interact", _interactBlock)

return block
