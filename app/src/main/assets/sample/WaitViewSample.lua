--
-- @author 17719
-- 2018/8/6 15:48
-- WaitViewSample.lua
--
require 'accessibility'

smartOpen("支付宝")
smartOpen("Alipay")
waitForApp('com.eg.android.AlipayGphone')

ViewFinder().equalsText('首页').id('tab_description').tryClick()
ViewFinder().equalsText('Home').id('tab_description').tryClick()
sacn = ViewFinder().id('saoyisao_tv')

sacn.waitFor().tryClick()
