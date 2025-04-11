-- A safe library resource
local M = {}
M.value = "safe_lib_loaded"
function M.greet(name)
  return "Hello from safe_lib, " .. (name or "guest")
end
return M