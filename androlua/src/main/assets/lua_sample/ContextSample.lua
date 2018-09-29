--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 12:00
-- ContextSample.lua
--
--local args = { ... } -- 外部参数 不需定义
print(app)
print(luaman)
print(bridges)
print(executor)
print(automator)
print(system)
print('currentApp',runtime.currentApp)--需无障碍权限
print('currentActivity',runtime.currentActivity)

print('actionCount',runtime.actionCount)
print('currentActionIndex: ',runtime.currentActionIndex)
print('actionScope: ',runtime.actionScope)--未指定 -1
print('isGlobal: ',runtime.isGlobal())
print('commandType: ',runtime.commandType)



print('size of args :',#args)
-- 输出
for i,v in ipairs(args) do
    print(i, v, type(v))
end
