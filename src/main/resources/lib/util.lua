local util = {
   entity = _require("util/entity")
}

function util._get_opt(tbl, key, default_value)
   return tbl[key] ~= nil and tbl[key] or default_value
end

function util.shuffle(tbl)
   for i = #tbl, 2, -1 do
      local j = math.random(i)
      tbl[i], tbl[j] = tbl[j], tbl[i]
   end
   return tbl
end

function util.take(n, tbl)
   if type(tbl) ~= "table" or type(n) ~= "number" then error("util.take requires exactly two arguments: n (number), tbl (table list)", 2) end
   if n > #tbl then error("util.take 'n' : " .. tostring(n) .. " must be smaller than the size of 'tbl' : " .. tostring(#tbl), 2) end
   
   local selection = {}
   for i=1, n do
      table.insert(selection, tbl[i])
   end
   return selection
end

return util
