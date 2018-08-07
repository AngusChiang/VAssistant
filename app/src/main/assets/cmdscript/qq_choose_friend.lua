--
-- @author 17719
-- 2018/8/6 23:53
-- qq_choose_friend.lua
--


ViewFinder().desc('返回消息').tryClick()
msg = ViewFinder().id('name').equalsText('消息').await()
msg.doubleClick()
s = ViewFinder().id('et_search_keyword')
s.tryClick()
sleep(110)
s.setTextWithInitial(args[1])
sleep(110)
a = ViewFinder().id('title').similaryText(args[1]).tryClick()
if (not a) then
    toast('没找到哦')
end

