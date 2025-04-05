local create_task_binding = _require("task/factory")._create_task_binding

local item = {}

item.pickup = create_task_binding("task.item.pickup", _pickup)
item.drop = create_task_binding("task.item.drop", _drop)
item.obtain = create_task_binding("task.item.obtain", _obtain)
item.eat = create_task_binding("task.item.eat", _eat)
item.smelt = create_task_binding("task.item.smelt", _smelt)

return item
