--
-- @author 17719
-- 2018/8/6 18:39
-- qq_scan.lua
--
require 'accessibility'

k = waitForDesc('快捷入口')
k.tryClick()
s = waitForDesc('扫一扫 按钮')
s.tryClick()