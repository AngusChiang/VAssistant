--
-- @author 17719247306
-- 2018/8/19 20:28
-- QqClearUnreadSample
--

require 'accessibility'
smartOpen('QQ')
ViewFinder().desc('返回消息').tryClick()

s = ViewFinder().id('name').equalsText('消息')
p = s.waitFor()
p = p.parent
function clear()
    local redPoint = p.childs[1] -- 第二个为红点点
    redPoint.swipe(200, -200, 500)
    --    local rect = redPoint.getBounds()
    --    local x = (rect.left + rect.right) / 2 --坐标
    --    local y = (rect.top + rect.bottom) / 2
    --    print(x, y)
    --    swipe(x, y, x + 200, y - 200, 500)
end

local cnum = #p.childs
-- print(cnum)
if (cnum == 3) then
    clear()
else speak("无未读消息")
end