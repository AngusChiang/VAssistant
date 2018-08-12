--
-- @author 17719
-- 2018/8/13 2:27
-- QQScanSample
--


require 'accessibility'
smartOpen('QQ')
sleep(110)
ViewFinder().desc('返回消息').tryClick()
k = waitForDesc('快捷入口')
k.tryClick()
s = waitForDesc('扫一扫 按钮')
s.tryClick()