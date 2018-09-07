--
-- @author 17719247306
-- 2018/8/16 0:49
-- view_op.lua
--
import 'cn.vove7.common.view.finder.ViewFindBuilder'
local ViewFindBuilder = luajava.bindClass('cn.vove7.common.view.finder.ViewFindBuilder')

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
    return ViewFinder().id(id).tryClick()
end

function clickText(text)
    return ViewFinder().equalsText(text).tryClick()
end

function clickByDesc(desc)
    return ViewFinder().desc(desc).tryClick()
end

function printAllChild(node)
    print(node)
    local cs = node.childs
    if (#cs == 0) then return end
    for i = 0, #cs - 1 do
        print(i, cs[i])
    end
end


function dd(d, i)
    local s = '-'
    for i = 0, d do
        s = s .. '-'
    end
    s = s .. i .. ' '
    return s
end


local function traversingNode(index, d, node)
    print(dd(d, index), node)
    local cs = node.childs
    for i = 0, #cs - 1 do
        traversingNode(i, d + 1, cs[i])
    end
end

function traversing(node)
    traversingNode(0, 0, node)
end