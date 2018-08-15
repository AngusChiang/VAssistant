--
-- @author 17719247306
-- 2018/8/16 0:49
-- view_op.lua
--
import 'cn.vove7.common.view.finder.ViewFindBuilder'
local ViewFindBuilder = luajava.bindClass('cn.vove7.common.view.finder.ViewFindBuilder')

--local bridges = luaman.getBridgeManager()

--[[
--视图节点查找器
 ]]
function ViewFinder()
    return ViewFindBuilder(executor)
end


import 'cn.vove7.common.accessibility.AccessibilityApi'
function rootView()
    local s = AccessibilityApi.Companion.getAccessibilityService()
    if (s) then
        return s.getRootViewNode()
    else return nil
    end
end


--[[
--快捷操作
 ]]
function clickById(id)
    ViewFinder().id(id).tryClick()
end

function clickText(text)
    ViewFinder().equalsText(text).tryClick()
end

function clickByDesc(desc)
    ViewFinder().desc(desc).tryClick()
end


