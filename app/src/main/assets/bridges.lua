--
-- @author Vove
-- Lua->Android调用桥
-- 2018/8/4 10:31
-- bridges/api.lua
--

require 'import'
import 'cn.vove7.androlua.androbridge.*'
import 'cn.vove7.common.view.finder.ViewFindBuilder'

--
bridges = luaman.getBridgeManager()
executor = bridges.getExecutor()
automator = bridges.getAutomator()
system = bridges.getSystemBridge()
resultNotifier = bridges.getResultNotifier()

--[[
--视图节点查找器
 ]]
function ViewFinder()
    return ViewFindBuilder(executor)
end

function EditableViewFinder()
    return ViewFindBuilder(executor)
end

--local accessibilityApi = bridges.accessibilityApi
--local automator = bridges.automator
--local serviceBridge = bridges.serviceBridge
--local systemBridge = bridges.systemBridge

function clickById(id)
    ViewFinder().id(id).tryClick()
end

function clickText(text)
    ViewFinder().equalsText(text).tryClick()
end

function clickByDesc(desc)
    ViewFinder().desc(desc).tryClick()
end

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

function toast(msg, l)
    automator.toast(msg, l)
end

function toast(msg)
    automator.toast(msg)
end

--  语音合成同步 ，说完再向下执行
-- @param text
-- @return booleam 是否成功
----
function speakSync(text)
    return executor.speakSync(text)
end

--
-- 语音合成
--
function speak(text)
    executor.speak(text)
end


--*
--通过包名打开App
-- ExResult
--
function openAppByPkg(pkg)
    return system.openAppByPkg(pkg)
end

--*
--通过通过关键字匹配
-- return ExResult pkgName if ok
--
function openAppByWord(appWord)
    return system.openAppByWord(appWord)
end

--*
--手电
--ExResult
function openFlashlight()
    system.openFlashlight()
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

function notifyFailed(s)
    resultNotifier.onExecuteFailed(s)
end

import 'android.os.Build'
function UserSdkVersion()
    return Build.VERSION.SDK_INT
end

function checkService()
    return executor.checkAccessibilityService(false)
end

import 'cn.vove7.common.accessibility.AccessibilityApi'
function rootView()
    local s = AccessibilityApi.Companion.getAccessibilityService()
    if (s) then
        return s.getRootViewNode()
    else return nil
    end
end

import 'cn.vove7.vtp.text.TextTransHelper'
function toPinyin(text)
    return TextTransHelper(app).chineseStr2Pinyin(text, false)
end

function toFirstPinyin(text)
    return TextTransHelper(app).chineseStr2Pinyin(text, true)
end
