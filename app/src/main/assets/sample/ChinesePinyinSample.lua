--
-- @author 17719
-- 2018/8/8 22:34
-- ChinesePinyinSample.lua
--

local datas = { '哈士奇', '萨摩耶', '柯基' }

for i, d in ipairs(datas) do
    print(i, toPinyin(d), toFirstPinyin(d))
end