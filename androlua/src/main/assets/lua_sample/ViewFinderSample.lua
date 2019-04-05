--
-- @author Vove
-- 2018/8/6 1:31
-- ViewFinderSample.lua
--

requireAccessibility()

a = ViewFinder().id('lua_args').findFirst()
for i = 0, 9 do
    a.setText('' .. i)
    sleep(500)
end