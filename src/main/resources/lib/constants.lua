local constants = {}

-- Utility function (optional, for cleaner logging)
local function count_keys(tbl)
  local count = 0
  if type(tbl) == "table" then
    for _ in pairs(tbl) do count = count + 1 end
  end
  return count
end

--- Populates a constants sub-table by calling :values() on enum class userdata
-- and then iterating the resulting array-like object using a numerical for loop.
-- @param target_key The name for the sub-table (e.g., "materials").
-- @param java_enum_class_userdata The global Lua variable holding the Java enum class userdata.
local function map_enum_array_to_lowercase(target_key, java_enum_class_userdata)
  --util.log_info("Mapping constants userdata for: " .. target_key)

  -- Check if the source global exists and is userdata
  if type(java_enum_class_userdata) ~= "userdata" then
    --util.log_warn("Cannot map constants for '" .. target_key .. "'. Global variable not found or not userdata. Type is: " .. type(java_enum_class_userdata))
    constants[target_key] = {}
    return
  end

  -- Attempt to call the :values() method to get the array-like object
  local get_values_ok, values_object_or_err = pcall(function() return java_enum_class_userdata:values() end)

  if not get_values_ok or values_object_or_err == nil then
    --util.log_warn("Could not call :values() on userdata for '" .. target_key .. "' or result was nil. Error/Result: " .. tostring(values_object_or_err))
    constants[target_key] = {}
    return
  end

  local values_object = values_object_or_err -- The object returned by :values()

  -- Attempt to get the size using the '#' operator first, wrapped in pcall
  local size = nil
  local get_len_ok, len_or_err = pcall(function() return #values_object end)
  if get_len_ok and type(len_or_err) == "number" then
      size = len_or_err
  else
      -- Fallback: Try calling a :size() method if '#' failed or returned non-number
      local get_size_ok, size_method_or_err = pcall(function() return values_object:size() end)
      if get_size_ok and type(size_method_or_err) == "number" then
           size = size_method_or_err
      end
  end

  -- Check if we obtained a valid size
  if not size or type(size) ~= "number" or size < 0 then
      --util.log_warn("Could not determine size (via '#' or :size()) for values returned by '" .. target_key .. ":values()'. Cannot iterate. '#'-Error: " .. tostring(len_or_err) .. ", :size()-Error: " .. tostring(size_method_or_err or 'N/A'))
      constants[target_key] = {}
      return
  end

  if size == 0 then
      --util.log_info("Enum '" .. target_key .. "' appears to be empty (size 0).")
      constants[target_key] = {}
      return
  end

  -- Initialize target table etc.
  constants[target_key] = {}
  local target_table = constants[target_key]
  local count = 0
  local errors = 0

  --util.log_info("Iterating " .. size .. " elements for '" .. target_key .. "'...")

  -- Iterate using a numerical for loop from 1 to size
  for index = 1, size do
    -- Access element by index, wrapped in pcall
    local get_value_ok, enum_value_or_err = pcall(function() return values_object[index] end)
    local enum_value = nil

    if get_value_ok and enum_value_or_err ~= nil then
        enum_value = enum_value_or_err
    else
        --util.log_warn("Error accessing index " .. index .. " for '" .. target_key .. "'. Error: " .. tostring(enum_value_or_err))
        errors = errors + 1
        enum_value = nil -- Ensure it's nil before checking below
    end

    -- Proceed only if we successfully got a non-nil value
    if enum_value then
        -- Try to call the .name() method on the enum userdata
        local get_name_ok, name_or_err = pcall(function() return enum_value:name() end)

        if get_name_ok and type(name_or_err) == "string" then
          local upper_name = name_or_err
          local lower_key = string.lower(upper_name)
          target_table[lower_key] = enum_value -- Assign the original enum value
          count = count + 1
        else
          --util.log_warn("Could not get valid name for constant at index " .. index .. " in '" .. target_key .. "'. Error/Result: " .. tostring(name_or_err))
          errors = errors + 1
        end
    end -- End if enum_value exists and is not nil
  end -- End for loop

  -- Logging (similar to before, reports counts and errors)
  if count > 0 then
     --util.log_info("Successfully mapped " .. count .. " constants to constants." .. target_key .. (errors > 0 and (" (" .. errors .. " errors).") or "."))
  else
     --util.log_warn("No constants successfully mapped for constants." .. target_key .. (errors > 0 and (" (" .. errors .. " errors).") or "."))
  end
   if count + errors ~= size then
       --util.log_warn("Mismatch in processed items for '" .. target_key .. "'. Expected size: " .. size .. ", Processed: " .. (count + errors))
   end
end

local function create_mappings()
   ----------------------------------------------------
-- Perform the Mapping for Required Enums
----------------------------------------------------
-- Ensure the globals like Material, EntityType etc. are loaded by Java bindings first!
   map_enum_array_to_lowercase("materials", Material)
   map_enum_array_to_lowercase("entities", Entity)
   map_enum_array_to_lowercase("biomes", Biome)
   map_enum_array_to_lowercase("effects", Effect) -- Assuming PotionEffectType is exposed as array
   --   map_enum_array_to_lowercase("enchantments", Enchantment) -- Assuming Enchantment is exposed as array
   map_enum_array_to_lowercase("damage_causes", DamageCause) -- Assuming DamageCause is exposed as array
   map_enum_array_to_lowercase("reward_types", RewardType) -- Assuming RewardType is exposed as array

   constants.enchantments = Enchantment -- Should already be a table exposed by java
----------------------------------------------------
-- Manually Add Custom Groups (Optional - Same as before)
   ----------------------------------------------------
--util.log_info("Adding custom constant groups...")
-- Materials Groups (Example)
constants.materials.Logs = {
  constants.materials.oak_log, constants.materials.spruce_log, constants.materials.birch_log,
  constants.materials.jungle_log, constants.materials.acacia_log, constants.materials.dark_oak_log,
  constants.materials.mangrove_log, constants.materials.cherry_log,
  constants.materials.crimson_stem, constants.materials.warped_stem
}
end
-- Add more groups...
create_mappings()

--util.log_info("Constants library initialization complete (array mode).")

-- The global 'constants' table is now populated and ready for use in board scripts.
return constants
