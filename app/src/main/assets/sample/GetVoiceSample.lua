--
-- @author 17719
-- 2018/8/7 3:05
-- GetVoiceSample.lua
--

s = waitForVoiceParam()
if (s) then
    print(s)
    toast(s)
else print('获取失败')
end
