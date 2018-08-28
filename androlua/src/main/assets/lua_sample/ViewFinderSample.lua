--
-- @author 17719
-- 2018/8/6 1:31
-- ViewFinderSample.lua
--

require 'accessibility'

a = ViewFinder().id('lua_args').findFirst()
for i = 0, 9 do
    a.setText('' .. i)
    sleep(500)
end