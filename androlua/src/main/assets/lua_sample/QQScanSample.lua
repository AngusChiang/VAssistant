--
-- @author 17719
-- 2018/8/13 2:27
-- QQScanSample
--


require 'accessibility'

--进入App 首页Activity
smartOpen('QQ')

--sleep(110)
ViewFinder().desc('返回消息').tryClick()  --防止在消息界面

msg = ViewFinder().id('name').equalsText('消息').await()
msg.doubleClick()
k = waitForDesc('快捷入口')
k.tryClick()
s = waitForDesc('扫一扫 按钮')
s.tryClick()