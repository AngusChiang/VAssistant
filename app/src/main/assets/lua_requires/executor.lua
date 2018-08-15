--
-- @author 17719247306
-- 2018/8/15 21:45
-- executor.lua
--


require 'import'
--
local bridges = luaman.getBridgeManager()
local executor = bridges.getExecutor()


function checkService()
    return executor.checkAccessibilityService(false)
end
--  语音合成同步 ，说完再向下执行
-- @param text
-- @return booleam 是否成功
--
function speakSync(text)
    return executor.speakSync(text)
end

--
-- 语音合成
--
function speak(text)
    executor.speak(text)
end

--
--
-- return boolean
function alert(t, m)
    return executor.alert(t, m)
end

--
--
-- return string
function waitForVoiceParam()
    return executor.waitForVoiceParam(nil)
end

--
-- s:询问时显示文字
-- return string
function waitForVoiceParam(s)
    return executor.waitForVoiceParam(s)
end

--
--
-- return boolean
function waitForApp(pkg)
    return executor.waitForApp(pkg, nil)
end

--
-- pkg, activity
-- return boolean
function waitForActivity(pkg, activity)
    return executor.waitForApp(pkg, activity)
end

--[[
-- return ViewNode? , null if AccessibilityService is not running
 ]]
function waitForId(id)
    return executor.waitForViewId(id)
end

--[[
-- return ViewNode? , null if AccessibilityService is not running
 ]]
function waitForDesc(desc)
    return executor.waitForDesc(desc)
end

--[[
-- return ViewNode? , null if AccessibilityService is not running
 ]]
function waitForText(text)
    return executor.waitForText(text)
end

--[[
-- smart smartOpen
 ]]
function smartOpen(s)
    return executor.smartOpen(s)
end

function sleep(m)
    executor.sleep(m)
end


