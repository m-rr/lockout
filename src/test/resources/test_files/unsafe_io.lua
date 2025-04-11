-- Tries to use IO
print("Attempting file IO...")
local f = io.open("sandbox_breach_test.txt", "w")
if f then
  f:write("unsafe write")
  f:close()
  return "IO_SUCCESS" -- Should not happen in sandbox
else
  return "IO_FAILED" -- Should not happen if io is nil
end
-- Script should fail before returning if io is nil