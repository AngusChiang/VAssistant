
print(app)
print(executor)
print(automator)
print(system)
print('currentApp',runtime.currentApp)//需无障碍权限
print('currentActivity',runtime.currentActivity)

print('actionCount',runtime.actionCount)
print('currentActionIndex: ',runtime.currentActionIndex)
print('actionScope: ',runtime.actionScope)//未指定 -1
print('isGlobal: ',runtime.isGlobal())//未指定 -1


print('size of args :',args.length)
// 输出
args.forEach(function(v,i) {
    print(i, v, typeof(v))
})
