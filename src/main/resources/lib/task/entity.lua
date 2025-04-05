local create_task_binding = _require("task/factory")._create_task_binding

local entity = {}

-- Assign the factory results to the specific helper functions
-- Assumes _kill, _hit etc are the base internal functions bound from Java
-- that create TaskMob instances for specific events.
--if _kill == nil then error("Missing internal binding _kill") end
entity.kill = create_task_binding("task.entity.kill", _kill)

--if not _hit then error("Missing internal binding _hit") end
entity.hit = create_task_binding("task.entity.hit", _hit)

--if not _tame then error("Missing internal binding _tame") end
entity.tame = create_task_binding("task.entity.tame", _tame)

--if not _shear then error("Missing internal binding _shear") end
entity.shear = create_task_binding("task.entity.shear", _shear)

--if not _bucket then error("Missing internal binding _bucket") end
entity.bucket_entity = create_task_binding("task.entity.bucket_entity", _bucket)

--if not _breed then error("Missing internal binding _breed") end
entity.breed = create_task_binding("task.entity.breed", _breed)

--if not _interactEntity then error("Missing internal binding _interactEntity") end
entity.interact = create_task_binding("task.entity.interact", _interactEntity) 

return entity
