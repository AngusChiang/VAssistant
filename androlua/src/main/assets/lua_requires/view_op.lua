--
-- @author Vove
-- 2018/8/16 0:49
-- view_op.lua
--
import 'cn.vove7.common.view.finder.ViewFindBuilder'
local ViewFindBuilder = luajava.bindClass('cn.vove7.common.view.finder.ViewFindBuilder')

--[[
--视图节点查找器
 ]]
function ViewFinder()
    return ViewFindBuilder()
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
    local s = ''
    for i = 1, d do
        s = s .. '  '
    end
    s = s .. '|-' .. i
    return s
end


local function traversingNode(index, d, node, printer)
    if (printer) then
        printer(node)
    else
        print(dd(d, index) .. ' ' .. node.toString())
    end
    local cs = node.childs
    for i = 0, #cs - 1 do
        if (cs[i].isVisibleToUser()) then
            traversingNode(i, d + 1, cs[i], printer)
        end
    end
end

function traversing(node, ...)
    local ps = { ... }
    local printer = ps[1]
    traversingNode(0, 0, node, printer)
end

-- 快捷api

function id(id)
    return ViewFinder().id(id)
end

function text(...)
    return ViewFinder().equalsText({...})
end

function containsText(...)
    return ViewFinder().containsText({...})
end

function matchesText(...)
    return ViewFinder().matchesText({...})
end

function similaryText(...)
    return ViewFinder().similaryText({...})
end

function desc(...)
    return ViewFinder().desc({...})
end

function containsDesc(...)
    return ViewFinder().containsDesc({...})
end

function editable(b)
    return ViewFinder().editable(b)
end

function scrollable(b)
    return ViewFinder().scrollable(b)
end

function type(...)
    return ViewFinder().type({ ... })
end

