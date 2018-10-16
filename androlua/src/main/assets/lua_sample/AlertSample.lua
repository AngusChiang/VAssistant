--
-- @author Vove
-- 2018/8/7 0:31
-- AlertSample.lua
--

if (alert('是否继续', '此时你可以通过语音来选择 取消 或者继续')) then
    toast('继续')
    print('继续')
else toast('取消')
end

