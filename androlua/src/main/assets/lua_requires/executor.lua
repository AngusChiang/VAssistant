--
-- @author 17719247306
-- 2018/8/15 21:45
-- executor.lua
--

require 'import'
--
local bridges = luaman.getBridgeManager()
local executor = bridges.getExecutor()
runtime = executor

actionCount = executor.actionCount
currentActionIndex = executor.currentActionIndex

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
-- return String?
function singleChoiceDialog(title, choices)
    return executor.singleChoiceDialog(title, choices)
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
-- 等待界面 pkg , activity  millis
-- return boolean
function waitForApp(pkg, ...)
    local ps = { ... }
    local activity
    local millis = -1
    if (#ps == 1) then
        if (type(ps[1]) == 'string') then
            activity = ps[1]
        elseif (type(ps[1]) == 'number') then
            millis = ps[1]
        end
    elseif (#ps == 2) then
        activity = ps[1]
        millis = ps[2]
    end
    log(activity)
    log(millis)
    return executor.waitForApp(pkg, activity, millis)
end


--[[
-- return ViewNode? , null if AccessibilityService is not running
 ]]
function waitForId(id, ...)
    local ps = { ... }
    local millis = -1
    if (#ps == 1) then
        millis = ps[1]
    end
    return executor.waitForViewId(id, millis)
end


--[[
-- return ViewNode? , null if AccessibilityService is not running
 ]]
function waitForDesc(desc, ...)
    local ps = { ... }
    local millis = -1
    if (#ps == 1) then
        millis = ps[1]
    end
    return executor.waitForDesc(desc, millis)
end

--[[
-- return ViewNode? , null if AccessibilityService is not running
 ]]
function waitForText(text, ...)
    local ps = { ... }
    local millis = -1
    if (#ps == 1) then
        millis = ps[1]
    end
    return executor.waitForText(text, millis)
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


