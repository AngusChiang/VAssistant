--
-- @author 17719
-- 2018/8/10 0:49
-- EditableFindSample.lua
--

require 'accessibility'

e = ViewFinder().editable().findFirst()
if (e) then
    e.setText('找到你啦')
else
    toast('没找到编辑框')
end

s = ViewFinder().scrollable().findFirst()
if (s) then
    s.scrollDown()
    s.scrollUp()
else
    toast('没找到可滑动的控件')
end
