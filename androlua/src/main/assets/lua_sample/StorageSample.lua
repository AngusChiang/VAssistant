--
-- @author Vove
-- 2018/8/16 22:15
-- StorageSample.lua
--

sp = SpHelper('lua_sample')

sp.set('no', 127)
sp.set('name', 'Lua')
sp.set('b', true)
local data = { 'a', 'b', 'c' }
set = SetBuilder().addAll(data)
sp.set('set', set.build())
--
print(sp.getInt('no'))
print(sp.getString('name'))
print(sp.getBoolean('b', false))
print(sp.getStringSet('set'))
