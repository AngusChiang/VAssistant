--
-- @author 17719
-- 2018/8/6 22:41
-- call.lua
--

local args = { ... }
if (#args >= 1) then
    executor.smartCallPhone(args[1])
end
