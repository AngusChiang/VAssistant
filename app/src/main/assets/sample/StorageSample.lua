--
-- @author 17719247306
-- 2018/8/16 22:15
-- StorageSample.lua
--

sp = SpHelper('sample')

sp.set('no', 127)
sp.set('name', 'Lua')
sp.set('b', true)
local data = { 'a', 'b', 'c' }
set = SetBuilder().addAll(data)
sp.set('set', set.build())
--
print(sp.getInt('no'))
print(sp.getString('name'))
print(sp.getBoolean('b'))
print(sp.getStringSet('set'))