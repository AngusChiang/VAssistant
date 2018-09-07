--
-- @author 17719247306
-- 2018/8/16 0:49
-- global_op.lua
--

--local deviceInfo = system.getDeviceInfo()
--local mHeight = deviceInfo.screenInfo.height
--local mWidth = deviceInfo.screenInfo.width
--
---- 相对度量
--local relHeight = mHeight
--local relWidth = mWidth

function setScreenSize(width, height)
    runtime.setScreenSize(width, height)
end

--local function (x)
--    local sx = x / relWidth * mWidth
--    log('x: ' .. sx)
--    return sx
--end
--
--local function (y)
--    local sy = y / relHeight * mHeight
--    log('y: ' .. sy)
--    return sy
--end

--[[
--全局操作
 ]]
function swipe(x1, y1, x2, y2, delay)
    return automator.swipe(x1, y1, x2, y2, delay)
end

function press(x, y, delay)
    return automator.press(x, y, delay)
end


function longClick(x, y)
    return automator.longClick(x, y)
end

function scrollDown()
    return automator.scrollDown()
end

function click(x, y)
    return automator.click(x, y)
end


function scrollUp()
    return automator.scrollUp()
end

function back()
    return automator.back()
end

function home()
    return automator.home()
end

function splitScreen()
    return automator.splitScreen()
end

function gesture(durtion, points)
    return automator.gesture(0, durtion, points)
end
--
--function scaleTable(points)
--    log('points size: ' .. #points)
--    local ps = {}
--    for i, v in ipairs(points) do
--        local x = (v.first)
--        local y = (v.second)
--        ps[i] = Pair(x, y)
--    end
--    return ps
--end

function gestureAsync(durtion, points)
    return automator.gestureAsync(0, durtion, points)
end

function powerDialog()
    return automator.powerDialog()
end

function notifications()
    return automator.notifications()
end

function quickSettings()
    return automator.quickSettings()
end

function recents()
    return automator.recents()
end

function toast(msg)
    automator.toast(msg)
end

