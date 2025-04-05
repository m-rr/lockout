--[[
 Helper functions to create configured TaskComponent objects using named arguments.
 These functions typically return the task object, which should then be added to the
 LockoutBoard.Tasks or LockoutBoard.Tiebreakers table by the script writer.
]]--

local task = {
   entity = _require("task/entity"),
   block = _require("task/block"),
   item = _require("task/item")
}

return task
