--
-- @author 17719
-- 2018/8/6 16:52
-- checkservice.lua
--
if (not executor.checkAccessibilityService(true).isSuccess) then
    return
end
print("...")