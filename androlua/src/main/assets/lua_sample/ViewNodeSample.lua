--
-- @author 17719
-- 2018/8/8 2:43
-- ViewNodeSample.lua
--

require 'accessibility'
root = rootView() --List

print(#root.childs)

function dd(d)
    local s = ''
    for i = 0, d do
        s = s .. ' '
    end
    return s
end

function t(d, node)
    print(dd(d), node)
    local cs = node.childs
    for i = 0, #cs - 1 do
        local child_i = cs[i]
        t(d + 1, child_i)
    end
end

t(0, root)