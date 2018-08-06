--
-- @author 17719
-- 2018/8/6 16:52
-- checkservice.lua
--

function checkservice()
    return executor.checkAccessibilityService(true).isSuccess
end