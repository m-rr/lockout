-- A regular script file
local safe = _require("safe_lib") -- Use _require for resource libs
local data = { message = "test_script_ran", lib_msg = safe.greet("Tester") }
return data