--
-- @author Vove
-- 2018/8/13 2:27
-- QQScanSample
--


require 'accessibility'

--进入App 首页
openAppByWord('QQ',true)

k = waitForDesc('快捷入口')
k.tryClick()
s = waitForDesc('扫一扫 按钮')
s.tryClick()