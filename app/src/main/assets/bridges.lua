--
-- @author Vove
-- Lua->Android调用桥
-- 2018/8/4 10:31
-- bridges/api.lua
--

require 'import'
import 'cn.vove7.androlua.androbridge.*'
import 'cn.vove7.jarvis.view.finder.*'

--
bridges = luaman.getBridgeManager()
executor = bridges.getExecutor()
automator = bridges.getAutomator()
function ViewFinder()
    return ViewFindBuilder(executor)
end

--local accessibilityApi = bridges.accessibilityApi
--local automator = bridges.automator
--local serviceBridge = bridges.serviceBridge
--local systemBridge = bridges.systemBridge


import 'cn.vove7.vtp.toast.Voast'


function swipe(x, y1, x2, y2, delay)
    automator.swipe(x, y1, x2, y2, delay)
end

function press(x, y, delay)
    automator.press(x, y, delay)
end


function longClick(x, y)
    automator.longClick(x, y)
end

function scrollDown()
    automator.scrollDown()
end

function click(x, y)
    automator.click(x, y)
end


function scrollUp()
    automator.scrollUp()
end

function back()
    automator.back()
end

function home()
    automator.home()
end

function powerDialog()
    automator.powerDialog()
end

function notifications()
    automator.notifications()
end

function quickSettings()
    automator.quickSettings()
end

function recents()
    automator.recents()
end


function sleep(m)
    executor.sleep(m)
end