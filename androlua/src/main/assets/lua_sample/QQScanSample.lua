--
-- @author Vove
-- 2018/8/13 2:27
-- QQScanSample
--


requireAccessibility()

--进入App 首页
system.openAppByWord('QQ',true)

k = waitForDesc('快捷入口')
k.tryClick()
s = waitForText('扫一扫')
s.tryClick()