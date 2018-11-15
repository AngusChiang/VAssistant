--
-- @author Vove
-- 2018/8/14 17:59
-- GestureSample.lua
-- 需要7.0+
--
require 'accessibility'
local path = { Pair(100, 100), Pair(200, 100), Pair(200, 200), Pair(100, 200), Pair(100, 100) }
toast('开启 开发者模式/指针位置 才能看出效果哦')
--gesture(4000, path)
local deviceInfo = system.getDeviceInfo()
local mHeight = deviceInfo.screenInfo.height
local mWidth = deviceInfo.screenInfo.width

--ρ=a(1-sinθ)
function heartLinePoints()
    local ps = {}
    local dx = mWidth / 2
    local dy = mHeight / 2
    local a = dx * 0.7
    for s = 0, 360 do
        local ss = math.rad(s)
        local p = (1 + math.sin(ss)) * a
        local x = Integer(p * math.cos(ss) + dx)
        local y = Integer(p * math.sin(ss) + dy)
        ps[s] = Pair(x, y)
    end
    return ps
end

gesture(7000, heartLinePoints())

