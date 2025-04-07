local util = {}

function util._get_opt(tbl, key, default_value)
   return tbl[key] ~= nil and tbl[key] or default_value
end

return util
