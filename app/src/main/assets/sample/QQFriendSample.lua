--
-- @author 17719
-- 2018/8/8 0:35
-- QQFriendSample.lua
--
require 'accessibility'
if (not openSomething('QQ')) then
    toast('没安装QQ哦')
    return
end

ViewFinder().desc('返回消息').tryClick()
msg = ViewFinder().id('name').equalsText('消息').await()
msg.doubleClick()
s = ViewFinder().id('et_search_keyword') -- s: ViewFinder类型
s.tryClick() --执行try 会搜索当前界面 并执行
sleep(110)

-- 注意此处 界面发生变化 需要重新搜索界面
-- 可以直接s.setTextWithInitial(args[1])
ViewFinder().id('et_search_keyword').setTextWithInitial(args[1])
--  由于之前保存过s 可以使用 ：s.setTextWithInitial(args[1])

sleep(110)
a = ViewFinder().id('title').similaryText(args[1]).tryClick()
if (not a) then
    toast('没找到哦')
end


--[[
-- 错误写法：
--
s = ViewFinder().id('et_search_keyword').findFirst() -- s: ViewNode类型
s.tryClick() --执行try 会搜索当前界面 并执行
sleep(110)

-- 错误写法： 因界面刷新，s图节点 已无法操作，返回失败
s.setTextWithInitial(args[1])

- -
--
 ]]