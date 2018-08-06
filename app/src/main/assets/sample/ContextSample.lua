--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 12:00
-- ContextSample.lua
--
local args = { ... } -- 外部参数
print(app)
print(luaman)
print(bridges)
print(executor)
print(automator)
print('size of args :',#args)
-- 输出
for i,v in ipairs(args) do
    print(i, v, type(v))
end

require 'checkservice'

a = ViewFindBuilder().id('stop').findFirst()
a.tryClick()