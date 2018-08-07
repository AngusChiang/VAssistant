--
-- @author 17719
-- 2018/8/6 16:52
-- checkservice.lua
--
if (not checkService()) then
    notifyFailed('无障碍未开启')
    return
else print('无障碍开启')
end