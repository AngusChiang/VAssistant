--
-- @author 17719
-- 2018/8/6 18:35
-- open.lua
--
local args = { ... }
if (#args >= 1) then
    executor.openSomething(args[1])
else
    print("打开什么鬼")
end

