--
-- @author 17719247306
-- 2018/8/16 23:04
-- ChoiceDialogSample.lua
--

s = singleChoiceDialog('选择', { '1', '2', '3' })
if (s) then
    print('选择了: ' .. s)
else
    print('取消')
end