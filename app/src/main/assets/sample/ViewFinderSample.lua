--
-- @author 17719
-- 2018/8/6 1:31
-- ViewFinderSample.lua
--

require 'accessibility'

a = ViewFinder().id('lua_args').findFirst()
a.setText('hello')
-- 还可以
-- ViewFinder().id('lua_args').setText('hello')