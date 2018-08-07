--
-- @author 17719
-- 2018/8/7 0:31
-- AlertSample.lua
--

if (alert('你好', '请选择,此时你可以通过语音来选择 取消 或者继续')) then
    toast('继续')
    print('继续')
else toast('停止')
end

