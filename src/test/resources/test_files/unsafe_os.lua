-- Tries to use OS
print("Attempting OS command...")
local result = os.execute("echo Unsafe OS command")
return result -- Should fail before returning if os is nil